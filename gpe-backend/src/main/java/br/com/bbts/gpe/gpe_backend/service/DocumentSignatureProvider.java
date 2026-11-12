package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.DocumentoVersao;
import org.springframework.core.io.Resource;

public interface DocumentSignatureProvider {
    SignatureResult submit(DocumentoVersao version, Resource content, String responsavelMatricula);
}
