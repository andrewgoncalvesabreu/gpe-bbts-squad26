package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoDTO(
        UUID id,
        UUID parceiroId,
        String parceiroNome,
        UUID parceriaId,
        String parceriaNome,
        String nome,
        String descricao,
        Integer versaoAtual,
        String statusAssinatura,
        LocalDateTime dataCriacao,
        DocumentoVersaoDTO versao) {
}
