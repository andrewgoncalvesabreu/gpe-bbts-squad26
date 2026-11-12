package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.AssinaturaDocumento;
import br.com.bbts.gpe.gpe_backend.domain.model.Documento;
import br.com.bbts.gpe.gpe_backend.domain.model.DocumentoVersao;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceria;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceriaRepository;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import org.springframework.core.io.ByteArrayResource;
import br.com.bbts.gpe.gpe_backend.domain.repository.AssinaturaDocumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.DocumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.DocumentoVersaoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.exception.SignatureIntegrationException;
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
class DocumentoServiceTest {
    @Mock private DocumentoRepository documentoRepository;
    @Mock private DocumentoVersaoRepository versaoRepository;
    @Mock private AssinaturaDocumentoRepository assinaturaRepository;
    @Mock private HistoricoParceiroRepository historicoRepository;
    @Mock private ParceriaRepository parceriaRepository;
    @Mock private ParceiroService parceiroService;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private InstrumentoRepository instrumentoRepository;
    @Mock private ArquivoDocumentoStorageService storage;
    @Mock private DocumentSignatureProvider signatureProvider;
    @InjectMocks private DocumentoService documentoService;

    @Test
    void rejectsPartnershipThatBelongsToAnotherPartnerBeforeStoringDocument() {
        UUID partnerId = UUID.randomUUID();
        UUID partnershipId = UUID.randomUUID();
        Parceiro partner = Parceiro.builder().id(partnerId).build();
        Parceria partnership = Parceria.builder()
                .id(partnershipId)
                .parceiro(Parceiro.builder().id(UUID.randomUUID()).build())
                .build();
        when(parceiroService.buscarPorId(partnerId)).thenReturn(partner);
        when(parceriaRepository.findById(partnershipId)).thenReturn(Optional.of(partnership));

        assertThrows(BusinessException.class, () -> documentoService.criarParaParceria(
                partnerId, partnershipId, "Contrato", null, "mat-101", null));
        verifyNoInteractions(storage);
    }

    @Test
    void storesSignatureProviderStatusAndProtocol() {
        UUID documentoId = UUID.randomUUID();
        Documento documento = documento(documentoId);
        DocumentoVersao versao = versao(documento);
        when(documentoRepository.findById(documentoId)).thenReturn(Optional.of(documento));
        when(versaoRepository.findByDocumentoIdAndNumeroVersao(documentoId, 2))
                .thenReturn(Optional.of(versao));
        when(storage.load(versao.getCaminhoArquivo())).thenReturn(new ByteArrayResource(new byte[] {1}));
        when(signatureProvider.submit(eq(versao), any(), eq("mat-101")))
                .thenReturn(new SignatureResult("MOCK", "AGUARDANDO_ASSINATURA", "mock-123", "accepted"));
        when(assinaturaRepository.save(any(AssinaturaDocumento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = documentoService.enviarParaAssinatura(documentoId, " mat-101 ");

        assertEquals("AGUARDANDO_ASSINATURA", documento.getStatusAssinatura());
        assertEquals("mock-123", result.protocolo());
        assertEquals(2, result.numeroVersao());
        assertEquals("mat-101", result.responsavelMatricula());
        verify(documentoRepository).save(documento);
    }

    @Test
    void recordsFailureAndMarksDocumentWhenProviderFails() {
        UUID documentoId = UUID.randomUUID();
        Documento documento = documento(documentoId);
        DocumentoVersao versao = versao(documento);
        when(documentoRepository.findById(documentoId)).thenReturn(Optional.of(documento));
        when(versaoRepository.findByDocumentoIdAndNumeroVersao(documentoId, 2))
                .thenReturn(Optional.of(versao));
        when(storage.load(versao.getCaminhoArquivo())).thenReturn(new ByteArrayResource(new byte[] {1}));
        when(signatureProvider.submit(eq(versao), any(), eq("mat-101")))
                .thenThrow(new SignatureIntegrationException("mock failure"));

        assertThrows(SignatureIntegrationException.class,
                () -> documentoService.enviarParaAssinatura(documentoId, "mat-101"));

        assertEquals("FALHA", documento.getStatusAssinatura());
        ArgumentCaptor<AssinaturaDocumento> history = ArgumentCaptor.forClass(AssinaturaDocumento.class);
        verify(assinaturaRepository).save(history.capture());
        assertEquals("FALHA", history.getValue().getStatus());
        assertEquals("mock failure", history.getValue().getDetalhe());
        verify(documentoRepository).save(documento);
    }

    private Documento documento(UUID id) {
        return Documento.builder()
                .id(id)
                .parceiro(Parceiro.builder().id(UUID.randomUUID()).razaoSocial("Parceiro").build())
                .nome("Contrato")
                .ativo(true)
                .versaoAtual(2)
                .statusAssinatura("NAO_ENVIADO")
                .build();
    }

    private DocumentoVersao versao(Documento documento) {
        return DocumentoVersao.builder()
                .id(UUID.randomUUID())
                .documento(documento)
                .numeroVersao(2)
                .nomeArquivo("contrato.pdf")
                .caminhoArquivo("doc/v2.pdf")
                .build();
    }
}
