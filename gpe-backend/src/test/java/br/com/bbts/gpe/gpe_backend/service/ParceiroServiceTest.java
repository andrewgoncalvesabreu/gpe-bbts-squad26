package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceiroRepository;
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
class ParceiroServiceTest {

    @Mock private ParceiroRepository parceiroRepository;
    @Mock private HistoricoParceiroRepository historicoRepository;
    @InjectMocks private ParceiroService parceiroService;

    @Test
    void registersCreationInPartnerHistoryWithResponsibleActor() {
        Parceiro parceiro = Parceiro.builder().cnpj("11222333000181").build();
        when(parceiroRepository.existsByCnpj(parceiro.getCnpj())).thenReturn(false);
        when(parceiroRepository.save(parceiro)).thenReturn(parceiro);

        Parceiro result = parceiroService.salvar(parceiro, "  mat-123  ");

        assertSame(parceiro, result);
        assertEquals("EM_ANALISE", result.getStatus());
        ArgumentCaptor<HistoricoParceiro> history = ArgumentCaptor.forClass(HistoricoParceiro.class);
        verify(historicoRepository).save(history.capture());
        assertEquals("Cadastro inicial do parceiro.", history.getValue().getDescricao());
        assertEquals("mat-123", history.getValue().getResponsavelMatricula());
        assertNotNull(history.getValue().getDataEvento());
    }

    @Test
    void recordsFieldsChangedDuringPartnerUpdate() {
        UUID id = UUID.randomUUID();
        Parceiro existing = Parceiro.builder()
                .id(id)
                .razaoSocial("Nome antigo")
                .cnpj("11222333000181")
                .status("ATIVO")
                .build();
        Parceiro updated = Parceiro.builder()
                .razaoSocial("Nome novo")
                .cnpj("11222333000181")
                .status("ATIVO")
                .build();
        when(parceiroRepository.findById(id)).thenReturn(Optional.of(existing));
        when(parceiroRepository.existsByCnpjAndIdNot(updated.getCnpj(), id)).thenReturn(false);
        when(parceiroRepository.save(existing)).thenReturn(existing);

        parceiroService.atualizar(id, updated, "mat-456");

        ArgumentCaptor<HistoricoParceiro> history = ArgumentCaptor.forClass(HistoricoParceiro.class);
        verify(historicoRepository).save(history.capture());
        assertTrue(history.getValue().getDescricao().contains("Razão Social"));
        assertEquals("mat-456", history.getValue().getResponsavelMatricula());
    }
}
