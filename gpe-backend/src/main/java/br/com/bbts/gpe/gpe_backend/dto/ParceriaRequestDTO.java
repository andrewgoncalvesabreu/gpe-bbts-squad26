package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ParceriaRequestDTO(
        @NotBlank @Size(max = 200) String nome,
        @NotNull UUID parceiroId,
        @NotNull UUID instrumentoId) {
}
