package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.exception.DocumentStorageException;
import br.com.bbts.gpe.gpe_backend.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipInputStream;

@Service
public class ArquivoDocumentoStorageService {
    public static final long MAX_SIZE_BYTES = 20L * 1024 * 1024;
    private final Path root;

    public ArquivoDocumentoStorageService(
            @Value("${gpe.storage.documentos:./storage/documentos}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível criar o diretório de documentos", exception);
        }
    }

    public StoredFile store(UUID documentoId, Integer numeroVersao, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new InvalidFileException("Selecione um arquivo não vazio");
        }
        if (arquivo.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("O arquivo excede o limite de 20 MB");
        }

        String originalName = safeFileName(arquivo.getOriginalFilename());
        int extensionPosition = originalName.lastIndexOf('.');
        if (extensionPosition <= 0) {
            throw new InvalidFileException("Formato de arquivo não permitido");
        }
        String extension = originalName.substring(extensionPosition).toLowerCase(Locale.ROOT);
        byte[] content;
        try {
            content = arquivo.getBytes();
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível ler o arquivo enviado", exception);
        }
        String mimeType = validateAndDetectMimeType(extension, content);

        String relative = documentoId + "/v" + numeroVersao + "-" + UUID.randomUUID() + extension;
        Path destination = resolve(relative);
        try {
            Files.createDirectories(destination.getParent());
            Files.write(destination, content);
            return new StoredFile(relative, originalName, mimeType, content.length, sha256(content));
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível armazenar o documento", exception);
        }
    }

    public Resource load(String relativePath) {
        Path path = resolve(relativePath);
        if (!Files.isRegularFile(path)) {
            throw new DocumentStorageException("O arquivo armazenado não foi encontrado",
                    new IOException("Missing stored document: " + relativePath));
        }
        try {
            return new UrlResource(path.toUri());
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível ler o documento", exception);
        }
    }

    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException exception) {
            throw new DocumentStorageException("Não foi possível remover um arquivo não confirmado", exception);
        }
    }

    private Path resolve(String relativePath) {
        Path path = root.resolve(relativePath).normalize();
        if (!path.startsWith(root)) {
            throw new InvalidFileException("Caminho de arquivo inválido");
        }
        return path;
    }

    private String validateAndDetectMimeType(String extension, byte[] content) {
        return switch (extension) {
            case ".pdf" -> {
                if (!startsWith(content, new byte[] {'%', 'P', 'D', 'F', '-'})) {
                    throw new InvalidFileException("O conteúdo não corresponde a um PDF");
                }
                yield "application/pdf";
            }
            case ".png" -> {
                byte[] signature = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
                if (!startsWith(content, signature)) {
                    throw new InvalidFileException("O conteúdo não corresponde a uma imagem PNG");
                }
                yield "image/png";
            }
            case ".jpg", ".jpeg" -> {
                if (content.length < 3 || (content[0] & 0xff) != 0xff
                        || (content[1] & 0xff) != 0xd8 || (content[2] & 0xff) != 0xff) {
                    throw new InvalidFileException("O conteúdo não corresponde a uma imagem JPG");
                }
                yield "image/jpeg";
            }
            case ".docx" -> {
                validateOfficePackage(content, "word/document.xml");
                yield "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            case ".xlsx" -> {
                validateOfficePackage(content, "xl/workbook.xml");
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

    public record StoredFile(String path, String originalName, String contentType, long size, String sha256) {
    }
}
