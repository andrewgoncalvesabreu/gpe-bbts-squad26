package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusWorkflowDTO {
    @NotBlank(message = "O novo status é obrigatório")
    private String novoStatus; // TRIAGEM, EM_AVALIACAO, APROVADA, REJEITADA, CANCELADA
}