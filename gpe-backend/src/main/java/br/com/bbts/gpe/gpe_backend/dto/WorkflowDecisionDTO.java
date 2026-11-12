package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkflowDecisionDTO {
    @NotBlank(message = "Matrícula do responsável é obrigatória")
    @Size(max = 50, message = "Matrícula deve ter no máximo 50 caracteres")
    private String responsavelMatricula;

    @Size(max = 2000, message = "Justificativa deve ter no máximo 2000 caracteres")
    private String justificativa;
}
