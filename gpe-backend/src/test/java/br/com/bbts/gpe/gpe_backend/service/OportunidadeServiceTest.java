package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.AvaliacaoOportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.dto.WorkflowDecisionDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OportunidadeServiceTest {

    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private AvaliacaoOportunidadeRepository avaliacaoRepository;
    @Mock private HistoricoParceiroRepository historicoRepository;
    @Mock private ParceiroService parceiroService;
    @InjectMocks private OportunidadeService oportunidadeService;

    private UUID oportunidadeId;
    private Parceiro parceiro;

    @BeforeEach
    void setUp() {
        oportunidadeId = UUID.randomUUID();
        parceiro = Parceiro.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void sendsEvaluatedOpportunityToApprovalAndRecordsActor() {
        Oportunidade oportunidade = oportunidade("EM_AVALIACAO");
        when(oportunidadeRepository.findById(oportunidadeId)).thenReturn(Optional.of(oportunidade));
        when(avaliacaoRepository.existsByOportunidadeId(oportunidadeId)).thenReturn(true);
        when(oportunidadeRepository.save(oportunidade)).thenReturn(oportunidade);
        WorkflowDecisionDTO request = request("mat-123", null);

        Oportunidade result = oportunidadeService.enviarParaAprovacao(oportunidadeId, request);

        assertEquals("EM_APROVACAO", result.getStatusWorkflow());
        ArgumentCaptor<HistoricoParceiro> history = ArgumentCaptor.forClass(HistoricoParceiro.class);
        verify(historicoRepository).save(history.capture());
        assertEquals("mat-123", history.getValue().getResponsavelMatricula());
        assertEquals("FLUXO_APROVACAO", history.getValue().getTipo());
    }

    @Test
    void doesNotSendOpportunityWithoutEvaluation() {
        Oportunidade oportunidade = oportunidade("EM_AVALIACAO");
        when(oportunidadeRepository.findById(oportunidadeId)).thenReturn(Optional.of(oportunidade));
        when(avaliacaoRepository.existsByOportunidadeId(oportunidadeId)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> oportunidadeService.enviarParaAprovacao(oportunidadeId, request("mat-123", null)));
        verify(oportunidadeRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void rejectsWithoutJustification() {
        assertThrows(BusinessException.class,
                () -> oportunidadeService.reprovar(oportunidadeId, request("mat-456", " ")));
        verify(oportunidadeRepository, never()).save(any());
    }

    @Test
    void approvesOnlyPendingOpportunityAndRecordsDecision() {
        Oportunidade oportunidade = oportunidade("EM_APROVACAO");
        when(oportunidadeRepository.findById(oportunidadeId)).thenReturn(Optional.of(oportunidade));
        when(oportunidadeRepository.save(oportunidade)).thenReturn(oportunidade);

        Oportunidade result = oportunidadeService.aprovar(oportunidadeId, request("mat-789", null));

        assertEquals("APROVADA", result.getStatusWorkflow());
        ArgumentCaptor<HistoricoParceiro> history = ArgumentCaptor.forClass(HistoricoParceiro.class);
        verify(historicoRepository).save(history.capture());
        assertEquals("Oportunidade aprovada.", history.getValue().getDescricao());
        assertEquals("mat-789", history.getValue().getResponsavelMatricula());
    }

    private Oportunidade oportunidade(String status) {
        return Oportunidade.builder()
                .id(oportunidadeId)
                .titulo("Parceria de teste")
                .statusWorkflow(status)
                .parceiro(parceiro)
                .build();
    }

    private WorkflowDecisionDTO request(String responsavel, String justificativa) {
        WorkflowDecisionDTO dto = new WorkflowDecisionDTO();
        dto.setResponsavelMatricula(responsavel);
        dto.setJustificativa(justificativa);
        return dto;
    }
}
