package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvaliacaoRequestDTO {

    @NotNull @Min(1) @Max(5)
    private Integer alinhamentoEstrategico;

    @NotNull @Min(1) @Max(5)
    private Integer potencialInovacao;

    @NotNull @Min(1) @Max(5)
    private Integer retornoFinanceiro;

    @NotNull @Min(1) @Max(5)
    private Integer impactoInstitucional;

    @NotNull @Min(1) @Max(5)
    private Integer viabilidadeTecnica;

    private String parecerTecnico;

    @NotBlank
    private String avaliadorMatricula;
}