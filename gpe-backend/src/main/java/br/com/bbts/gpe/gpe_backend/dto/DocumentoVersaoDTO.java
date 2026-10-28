package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoVersaoDTO(
        UUID id,
        Integer numeroVersao,
        String nomeArquivo,
        String contentType,
        Long tamanhoBytes,
        String hashSha256,
        LocalDateTime dataEnvio) {
}