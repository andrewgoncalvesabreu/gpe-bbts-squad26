package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InstrumentoAlertaDTO(
        UUID instrumentoId,
        String instrumentoNome,
        String parceiroNome,
        LocalDate dataVencimento,
        long diasAteVencimento) {
}
