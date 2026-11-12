package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssinaturaDocumentoDTO(
        UUID id,
        Integer numeroVersao,
        String provedor,
        String status,
        String protocolo,
        String detalhe,
        String responsavelMatricula,
        LocalDateTime dataEvento) {
}
