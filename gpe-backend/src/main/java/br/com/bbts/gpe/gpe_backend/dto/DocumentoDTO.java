package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoDTO(
        UUID id,
        String nome,
        String descricao,
        UUID parceiroId,
        String parceiroNome,
        UUID oportunidadeId,
        String oportunidadeTitulo,
        UUID instrumentoId,
        String instrumentoNome,
        UUID parceriaId,
        String parceriaNome,
        Integer versaoAtual,
        String nomeArquivoAtual,
        Long tamanhoArquivoAtual,
        String statusAssinatura,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        DocumentoVersaoDTO versao) {
}
