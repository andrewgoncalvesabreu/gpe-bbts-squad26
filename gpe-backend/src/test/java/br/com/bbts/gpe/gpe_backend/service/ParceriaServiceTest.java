package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceriaRepository;
import br.com.bbts.gpe.gpe_backend.dto.ParceriaRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParceriaServiceTest {
    @Mock private ParceriaRepository parceriaRepository;
    @Mock private InstrumentoRepository instrumentoRepository;
    @Mock private ParceiroService parceiroService;
    @InjectMocks private ParceriaService parceriaService;

    @Test
    void rejectsInstrumentThatBelongsToAnotherPartner() {
        UUID partnerId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        Parceiro partner = Parceiro.builder().id(partnerId).status("ATIVO").build();
        InstrumentoJuridico instrument = InstrumentoJuridico.builder()
                .id(instrumentId)
                .parceiro(Parceiro.builder().id(UUID.randomUUID()).build())
                .build();
        when(parceiroService.buscarPorId(partnerId)).thenReturn(partner);
        when(instrumentoRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));

        assertThrows(BusinessException.class, () -> parceriaService.criar(
                new ParceriaRequestDTO("Parceria", partnerId, instrumentId)));
        verify(parceriaRepository, never()).save(any());
    }
}
