package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParceriaDTO(
        UUID id,
        String nome,
        UUID parceiroId,
        String parceiroNome,
        UUID instrumentoId,
        String instrumentoNome,
        String status,
        LocalDateTime dataCriacao) {
}
