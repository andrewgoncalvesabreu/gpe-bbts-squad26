package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.TipoInstrumento;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.domain.model.ProrrogacaoInstrumento;
import br.com.bbts.gpe.gpe_backend.domain.repository.ProrrogacaoInstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.dto.ProrrogacaoInstrumentoRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentoServiceTest {

    @Mock private InstrumentoRepository instrumentoRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private ProrrogacaoInstrumentoRepository prorrogacaoRepository;
    @Mock private ParceiroService parceiroService;
    @InjectMocks private InstrumentoService instrumentoService;

    @Test
    void savesInstrumentLinkedToPartnerAndOpportunity() {
        UUID partnerId = UUID.randomUUID();
        UUID opportunityId = UUID.randomUUID();
        Parceiro partner = Parceiro.builder().id(partnerId).build();
        Oportunidade opportunity = Oportunidade.builder()
                .id(opportunityId)
                .parceiro(partner)
                .build();
        InstrumentoJuridico submitted = InstrumentoJuridico.builder()
                .nome("Acordo de cooperação")
                .tipo(TipoInstrumento.ACORDO_COOPERACAO)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .parceiro(Parceiro.builder().id(partnerId).build())
                .oportunidade(Oportunidade.builder().id(opportunityId).build())
                .build();

        when(parceiroService.buscarPorId(partnerId)).thenReturn(partner);
        when(oportunidadeRepository.findById(opportunityId)).thenReturn(Optional.of(opportunity));
        when(instrumentoRepository.save(submitted)).thenReturn(submitted);

        InstrumentoJuridico result = instrumentoService.salvar(submitted);

        assertSame(partner, result.getParceiro());
        assertSame(opportunity, result.getOportunidade());
        verify(instrumentoRepository).save(submitted);
    }

    @ParameterizedTest
    @EnumSource(TipoInstrumento.class)
    void savesEveryInstrumentType(TipoInstrumento tipo) {
        UUID partnerId = UUID.randomUUID();
        Parceiro partner = Parceiro.builder().id(partnerId).build();
        InstrumentoJuridico submitted = InstrumentoJuridico.builder()
                .nome("Instrumento de teste")
                .tipo(tipo)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .parceiro(Parceiro.builder().id(partnerId).build())
                .build();

        when(parceiroService.buscarPorId(partnerId)).thenReturn(partner);
        when(instrumentoRepository.save(submitted)).thenReturn(submitted);

        InstrumentoJuridico result = instrumentoService.salvar(submitted);

        assertEquals(tipo, result.getTipo());
        assertSame(partner, result.getParceiro());
        verify(instrumentoRepository).save(submitted);
    }

    @Test
    void rejectsOpportunityBelongingToAnotherPartner() {
        Parceiro selectedPartner = Parceiro.builder().id(UUID.randomUUID()).build();
        Parceiro otherPartner = Parceiro.builder().id(UUID.randomUUID()).build();
        UUID opportunityId = UUID.randomUUID();
        InstrumentoJuridico submitted = InstrumentoJuridico.builder()
                .nome("Contrato")
                .tipo(TipoInstrumento.CONTRATO)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .parceiro(Parceiro.builder().id(selectedPartner.getId()).build())
                .oportunidade(Oportunidade.builder().id(opportunityId).build())
                .build();

        when(parceiroService.buscarPorId(selectedPartner.getId())).thenReturn(selectedPartner);
        when(oportunidadeRepository.findById(opportunityId))
                .thenReturn(Optional.of(Oportunidade.builder().parceiro(otherPartner).build()));

        assertThrows(BusinessException.class, () -> instrumentoService.salvar(submitted));
        verify(instrumentoRepository, never()).save(any());
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        InstrumentoJuridico submitted = InstrumentoJuridico.builder()
                .dataInicioVigencia(LocalDate.of(2026, 5, 1))
                .dataFimVigencia(LocalDate.of(2026, 4, 30))
                .build();

        assertThrows(BusinessException.class, () -> instrumentoService.salvar(submitted));
        verify(instrumentoRepository, never()).save(any());
    }

    @Test
    void extendsValidityAndRecordsPreviousAndNewEndDates() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentoJuridico instrumento = InstrumentoJuridico.builder()
                .id(instrumentId)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .build();
        ProrrogacaoInstrumentoRequestDTO request = new ProrrogacaoInstrumentoRequestDTO();
        request.setNovaDataFim(LocalDate.of(2027, 6, 30));
        request.setJustificativa("Continuidade do projeto");
        when(instrumentoRepository.findById(instrumentId)).thenReturn(Optional.of(instrumento));
        when(prorrogacaoRepository.save(any(ProrrogacaoInstrumento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProrrogacaoInstrumento result = instrumentoService.prorrogar(instrumentId, request);

        assertEquals(LocalDate.of(2027, 6, 30), instrumento.getDataFimVigencia());
        assertEquals(LocalDate.of(2026, 12, 31), result.getDataFimAnterior());
        assertEquals(LocalDate.of(2027, 6, 30), result.getDataFimNova());
        assertEquals("Continuidade do projeto", result.getJustificativa());
        verify(instrumentoRepository).save(instrumento);
        verify(prorrogacaoRepository).save(result);
    }

    @Test
    void rejectsProrrogationThatDoesNotExtendCurrentTerm() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentoJuridico instrumento = InstrumentoJuridico.builder()
                .id(instrumentId)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .build();
        ProrrogacaoInstrumentoRequestDTO request = new ProrrogacaoInstrumentoRequestDTO();
        request.setNovaDataFim(LocalDate.of(2026, 12, 31));
        when(instrumentoRepository.findById(instrumentId)).thenReturn(Optional.of(instrumento));

        assertThrows(BusinessException.class, () -> instrumentoService.prorrogar(instrumentId, request));
        verify(instrumentoRepository, never()).save(any());
        verify(prorrogacaoRepository, never()).save(any());
    }
}
