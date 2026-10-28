package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ArquivoDocumentoStorageService {
    private final Path root;

    public ArquivoDocumentoStorageService(
            @Value("${gpe.storage.documentos:./storage/documentos}") String root) {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar o diretório de documentos", e);
        }
    }

    public StoredFile store(UUID documentoId, Integer numeroVersao, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Selecione um arquivo para enviar");
        }
        String nome = StringUtils.cleanPath(arquivo.getOriginalFilename() == null
                ? "arquivo"
                : arquivo.getOriginalFilename());
        if (nome.contains("..")) {
            throw new BusinessException("Nome de arquivo inválido");
        }
        String relative = documentoId + "/v" + numeroVersao + "-" + UUID.randomUUID() + "-" + nome;
        Path destination = root.resolve(relative).normalize();
        if (!destination.startsWith(root)) {
            throw new BusinessException("Caminho de arquivo inválido");
        }
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(arquivo.getInputStream(), destination);
            return new StoredFile(relative, nome, contentType(arquivo), arquivo.getSize(), sha256(destination));
        } catch (IOException e) {
            throw new BusinessException("Não foi possível armazenar o documento");
        }
    }

    public Resource load(String relativePath) {
        try {
            Path path = root.resolve(relativePath).normalize();
            if (!path.startsWith(root) || !Files.exists(path)) {
                throw new BusinessException("Arquivo do documento não encontrado");
            }
            return new UrlResource(path.toUri());
        } catch (IOException e) {
            throw new BusinessException("Não foi possível ler o documento");
        }
    }

    private String contentType(MultipartFile arquivo) {
        return arquivo.getContentType() == null || arquivo.getContentType().isBlank()
                ? "application/octet-stream"
                : arquivo.getContentType();
    }

    private String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public record StoredFile(String path, String originalName, String contentType, long size, String sha256) {}
}