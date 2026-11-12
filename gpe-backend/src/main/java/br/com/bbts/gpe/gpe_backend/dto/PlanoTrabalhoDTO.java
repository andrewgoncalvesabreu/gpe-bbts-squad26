package br.com.bbts.gpe.gpe_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PlanoTrabalhoDTO(
        UUID id,
        UUID projetoId,
        List<String> objetivos,
        LocalDate cronogramaInicio,
        LocalDate cronogramaFim,
        String responsaveis,
        List<EntregaDTO> entregas,
        List<MarcoDTO> marcos,
        LocalDateTime dataCriacao) {

    public record EntregaDTO(UUID id, String descricao, LocalDate dataPrevista, String responsavel) {
    }

    public record MarcoDTO(UUID id, String descricao, LocalDate dataPrevista) {
    }
}
