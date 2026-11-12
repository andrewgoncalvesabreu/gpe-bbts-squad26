package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.DocumentoVersao;
import br.com.bbts.gpe.gpe_backend.exception.SignatureIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class MockDocumentSignatureProvider implements DocumentSignatureProvider {
    private final boolean simulateFailure;
    private final String resultStatus;

    public MockDocumentSignatureProvider(
            @Value("${app.signature.mock.failure:false}") boolean simulateFailure,
            @Value("${app.signature.mock.result-status:AGUARDANDO_ASSINATURA}") String resultStatus) {
        this.simulateFailure = simulateFailure;
        this.resultStatus = resultStatus;
    }

    @Override
    public SignatureResult submit(DocumentoVersao version, Resource content, String responsavelMatricula) {
        if (simulateFailure) {
            throw new SignatureIntegrationException("Falha simulada pelo provedor de assinatura mock");
        }
        try {
            if (content == null || !content.isReadable() || content.contentLength() == 0) {
                throw new SignatureIntegrationException("O arquivo não está disponível para assinatura");
            }
        } catch (IOException exception) {
            throw new SignatureIntegrationException("Não foi possível ler o arquivo para assinatura", exception);
        }
        List<String> statuses = List.of("AGUARDANDO_ASSINATURA", "ASSINADO", "RECUSADO", "CANCELADO");
        if (!statuses.contains(resultStatus)) {
            throw new SignatureIntegrationException("Status de retorno inválido no provedor de assinatura mock");
        }
        return new SignatureResult(
                "MOCK",
                resultStatus,
                "MOCK-" + UUID.randomUUID(),
                "Retorno simulado para a versão " + version.getNumeroVersao()
                        + " por " + responsavelMatricula + "; não possui validade jurídica.");
    }
}
