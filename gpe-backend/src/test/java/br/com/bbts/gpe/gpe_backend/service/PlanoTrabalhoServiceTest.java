package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.Projeto;
import br.com.bbts.gpe.gpe_backend.domain.repository.PlanoTrabalhoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ProjetoRepository;
import br.com.bbts.gpe.gpe_backend.dto.PlanoTrabalhoRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoTrabalhoServiceTest {
    @Mock private PlanoTrabalhoRepository planoRepository;
    @Mock private ProjetoRepository projetoRepository;
    @InjectMocks private PlanoTrabalhoService planoService;

    @Test
    void rejectsDeliverablesOutsideProjectSchedule() {
        UUID projectId = UUID.randomUUID();
        when(projetoRepository.findById(projectId))
                .thenReturn(Optional.of(Projeto.builder().id(projectId).build()));
        PlanoTrabalhoRequestDTO request = new PlanoTrabalhoRequestDTO(
                java.util.List.of("Objetivo"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "Equipe",
                java.util.List.of(new PlanoTrabalhoRequestDTO.EntregaRequestDTO(
                        "Entrega", LocalDate.of(2027, 1, 1), "Responsável")),
                java.util.List.of());

        assertThrows(BusinessException.class, () -> planoService.criar(projectId, request));
        verify(planoRepository, never()).save(any());
    }

    @Test
    void persistsObjectivesDeliverablesAndMilestonesForProject() {
        UUID projectId = UUID.randomUUID();
        Projeto project = Projeto.builder().id(projectId).build();
        when(projetoRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(planoRepository.findByProjetoId(projectId)).thenReturn(Optional.empty());
        when(planoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PlanoTrabalhoRequestDTO request = new PlanoTrabalhoRequestDTO(
                java.util.List.of("Objetivo 1", "Objetivo 2"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "Equipe responsável",
                java.util.List.of(new PlanoTrabalhoRequestDTO.EntregaRequestDTO(
                        "Relatório", LocalDate.of(2026, 6, 1), "Pessoa 1")),
                java.util.List.of(new PlanoTrabalhoRequestDTO.MarcoRequestDTO(
                        "Marco inicial", LocalDate.of(2026, 2, 1))));

        var result = planoService.criar(projectId, request);

        assertEquals(projectId, result.projetoId());
        assertEquals(2, result.objetivos().size());
        assertEquals("Relatório", result.entregas().getFirst().descricao());
        assertEquals("Marco inicial", result.marcos().getFirst().descricao());
    }
}
