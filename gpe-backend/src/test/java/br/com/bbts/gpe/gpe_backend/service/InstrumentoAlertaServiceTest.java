package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.dto.InstrumentoAlertaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentoAlertaServiceTest {

    @Mock
    private InstrumentoRepository instrumentoRepository;

    @InjectMocks
    private InstrumentoAlertaService alertaService;

    @Test
    void listsInstrumentsWithinThirtyDaysAndExcludesExpiredOnes() {
        LocalDate hoje = LocalDate.of(2026, 10, 21);
        InstrumentoJuridico venceHoje = instrumento("Vence hoje", hoje);
        InstrumentoJuridico venceEmTrintaDias = instrumento("Vence em 30 dias", hoje.plusDays(30));
        InstrumentoJuridico vencido = instrumento("Já vencido", hoje.minusDays(1));
        when(instrumentoRepository
                .findByDataFimVigenciaGreaterThanEqualAndDataFimVigenciaLessThanEqualOrderByDataFimVigenciaAsc(
                        hoje, hoje.plusDays(30)))
                .thenReturn(List.of(venceHoje, venceEmTrintaDias, vencido));

        List<InstrumentoAlertaDTO> alertas = alertaService.listarProximosVencimentos(hoje);

        assertEquals(2, alertas.size());
        assertEquals("Vence hoje", alertas.get(0).instrumentoNome());
        assertEquals(0, alertas.get(0).diasAteVencimento());
        assertEquals("Vence em 30 dias", alertas.get(1).instrumentoNome());
        assertEquals(30, alertas.get(1).diasAteVencimento());
    }

    @Test
    void queriesOnlyFromTodayThroughTheThirtyDayWindow() {
        LocalDate hoje = LocalDate.of(2026, 10, 21);

        alertaService.listarProximosVencimentos(hoje);

        verify(instrumentoRepository)
                .findByDataFimVigenciaGreaterThanEqualAndDataFimVigenciaLessThanEqualOrderByDataFimVigenciaAsc(
                        hoje, hoje.plusDays(30));
    }

    private InstrumentoJuridico instrumento(String nome, LocalDate vencimento) {
        return InstrumentoJuridico.builder()
                .id(UUID.randomUUID())
                .nome(nome)
                .dataFimVigencia(vencimento)
                .parceiro(Parceiro.builder().razaoSocial("Parceiro teste").build())
                .build();
    }
}
