package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssinaturaRequestDTO(
        @NotBlank(message = "Matrícula do responsável é obrigatória")
        @Size(max = 50, message = "Matrícula deve ter no máximo 50 caracteres")
        String responsavelMatricula) {
}
