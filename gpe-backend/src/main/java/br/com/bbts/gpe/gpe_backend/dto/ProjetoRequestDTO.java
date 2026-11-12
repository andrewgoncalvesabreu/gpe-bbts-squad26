package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProjetoRequestDTO(
        @NotNull UUID parceriaId,
        @NotBlank @Size(max = 200) String nome,
        @NotBlank String descricao) {
}
