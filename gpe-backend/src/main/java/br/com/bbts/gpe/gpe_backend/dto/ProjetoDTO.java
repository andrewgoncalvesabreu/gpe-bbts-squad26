package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjetoDTO(
        UUID id,
        UUID parceriaId,
        String parceriaNome,
        String parceiroNome,
        String nome,
        String descricao,
        String status,
        LocalDateTime dataCriacao,
        boolean possuiPlanoTrabalho) {
}
