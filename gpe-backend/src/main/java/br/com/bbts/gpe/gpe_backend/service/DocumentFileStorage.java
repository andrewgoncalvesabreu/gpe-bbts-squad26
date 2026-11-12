package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.exception.DocumentStorageException;
import br.com.bbts.gpe.gpe_backend.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipInputStream;

@Component
public class DocumentFileStorage {
    public static final long MAX_SIZE_BYTES = 20L * 1024 * 1024;
    private final Path root;

    public DocumentFileStorage(
            @Value("${app.document.storage-directory:./data/documents}") String storageDirectory) {
        this.root = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public StoredDocumentFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Selecione um arquivo não vazio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("O arquivo excede o limite de 20 MB");
        }

        String originalName = safeFileName(file.getOriginalFilename());
        int extensionPosition = originalName.lastIndexOf('.');
        if (extensionPosition <= 0) {
            throw new InvalidFileException("Formato de arquivo não permitido");
        }
        String extension = originalName.substring(extensionPosition).toLowerCase(Locale.ROOT);
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível ler o arquivo enviado", exception);
        }
        String mimeType = validateAndDetectMimeType(extension, content);

        String key = UUID.randomUUID() + extension;
        Path destination = resolve(key);
        try {
            Files.createDirectories(root);
            Files.write(destination, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return new StoredDocumentFile(key, mimeType, content.length, sha256(content));
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível armazenar o arquivo", exception);
        }
    }

    public Resource load(String storageKey) {
        Path path = resolve(storageKey);
        if (!Files.isRegularFile(path)) {
            throw new DocumentStorageException("O arquivo armazenado não foi encontrado",
                    new IOException("Missing stored document: " + storageKey));
        }
        return new FileSystemResource(path);
    }

    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível remover um arquivo não confirmado", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path path = root.resolve(storageKey).normalize();
        if (!path.getParent().equals(root)) {
            throw new InvalidFileException("Chave de armazenamento inválida");
        }
        return path;
    }

    private String validateAndDetectMimeType(String extension, byte[] header) {
        return switch (extension) {
            case ".pdf" -> {
                if (!startsWith(header, new byte[] {'%', 'P', 'D', 'F', '-'})) {
                    throw new InvalidFileException("O conteúdo não corresponde a um PDF");
                }
                yield "application/pdf";
            }
            case ".png" -> {
                byte[] signature = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
                if (!startsWith(header, signature)) {
                    throw new InvalidFileException("O conteúdo não corresponde a uma imagem PNG");
                }
                yield "image/png";
            }
            case ".jpg", ".jpeg" -> {
                if (header.length < 3 || (header[0] & 0xff) != 0xff
                        || (header[1] & 0xff) != 0xd8 || (header[2] & 0xff) != 0xff) {
                    throw new InvalidFileException("O conteúdo não corresponde a uma imagem JPG");
                }
                yield "image/jpeg";
            }
            case ".docx" -> {
                validateOfficePackage(header, "word/document.xml");
                yield "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            case ".xlsx" -> {
                validateOfficePackage(header, "xl/workbook.xml");
                yield "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }
            default -> throw new InvalidFileException("Formato não permitido. Use PDF, DOCX, XLSX, PNG ou JPG");
        };
    }

    private void validateOfficePackage(byte[] content, String requiredEntry) {
        boolean validPackage = false;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String entryName = entry.getName();
                int separator = Math.max(entryName.lastIndexOf('/'), entryName.lastIndexOf('\\'));
                if ("vbaProject.bin".equalsIgnoreCase(entryName.substring(separator + 1))) {
                    throw new InvalidFileException("Documentos Office com macros não são permitidos");
                }
                if (requiredEntry.equals(entryName)) {
                    validPackage = true;
                }
            }
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível verificar o arquivo Office enviado", exception);
        }
        if (!validPackage) {
            throw new InvalidFileException("O conteúdo não corresponde a um documento Office válido");
        }
    }

    private String safeFileName(String fileName) {
        String normalized = fileName == null ? "" : fileName.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        return normalized.substring(separator + 1).replaceAll("[\\p{Cntrl}]", "_");
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (value[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM", exception);
        }
    }
}
