package br.com.bbts.gpe.gpe_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProrrogacaoInstrumentoRequestDTO {

    @NotNull(message = "Nova data de término é obrigatória")
    private LocalDate novaDataFim;

    @Size(max = 2000, message = "Justificativa deve ter no máximo 2000 caracteres")
    private String justificativa;
}
