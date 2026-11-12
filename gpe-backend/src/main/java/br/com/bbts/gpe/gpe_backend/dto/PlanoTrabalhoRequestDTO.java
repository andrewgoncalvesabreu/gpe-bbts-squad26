package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PlanoTrabalhoRequestDTO(
        @NotEmpty List<@NotBlank @Size(max = 1000) String> objetivos,
        @NotNull LocalDate cronogramaInicio,
        @NotNull LocalDate cronogramaFim,
        @NotBlank String responsaveis,
        @Valid List<EntregaRequestDTO> entregas,
        @Valid List<MarcoRequestDTO> marcos) {

    public record EntregaRequestDTO(
            @NotBlank String descricao,
            @NotNull LocalDate dataPrevista,
            @NotBlank @Size(max = 100) String responsavel) {
    }

    public record MarcoRequestDTO(
            @NotBlank String descricao,
            @NotNull LocalDate dataPrevista) {
    }
}
