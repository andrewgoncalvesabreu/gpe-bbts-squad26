package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.AssinaturaDocumento;
import br.com.bbts.gpe.gpe_backend.domain.model.Documento;
import br.com.bbts.gpe.gpe_backend.domain.model.DocumentoVersao;
import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.AssinaturaDocumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.DocumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.DocumentoVersaoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceriaRepository;
import br.com.bbts.gpe.gpe_backend.dto.AssinaturaDocumentoDTO;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoDTO;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoVersaoDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import br.com.bbts.gpe.gpe_backend.exception.SignatureIntegrationException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {
    private final DocumentoRepository documentoRepository;
    private final DocumentoVersaoRepository versaoRepository;
    private final AssinaturaDocumentoRepository assinaturaRepository;
    private final HistoricoParceiroRepository historicoRepository;
    private final ParceriaRepository parceriaRepository;
    private final ParceiroService parceiroService;
    private final DocumentFileStorage storage;
    private final DocumentSignatureProvider signatureProvider;

    public DocumentoService(
            DocumentoRepository documentoRepository,
            DocumentoVersaoRepository versaoRepository,
            AssinaturaDocumentoRepository assinaturaRepository,
            HistoricoParceiroRepository historicoRepository,
            ParceriaRepository parceriaRepository,
            ParceiroService parceiroService,
            DocumentFileStorage storage,
            DocumentSignatureProvider signatureProvider) {
        this.documentoRepository = documentoRepository;
        this.versaoRepository = versaoRepository;
        this.assinaturaRepository = assinaturaRepository;
        this.historicoRepository = historicoRepository;
        this.parceriaRepository = parceriaRepository;
        this.parceiroService = parceiroService;
        this.storage = storage;
        this.signatureProvider = signatureProvider;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarPorParceiro(UUID parceiroId) {
        parceiroService.buscarPorId(parceiroId);
        return documentoRepository.findByParceiroIdOrderByDataCriacaoDesc(parceiroId)
                .stream().map(this::toDocumentoDTO).toList();
    }

    @Transactional
    public DocumentoDTO criar(UUID parceiroId, UUID parceriaId, String nome, String descricao,
            String responsavelMatricula, MultipartFile file) {
        validarResponsavel(responsavelMatricula);
        Parceiro parceiro = parceiroService.buscarPorId(parceiroId);
        var parceria = parceriaRepository.findById(parceriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parceria não encontrada: " + parceriaId));
        if (!parceiro.getId().equals(parceria.getParceiro().getId())) {
            throw new BusinessException("A parceria selecionada não pertence ao parceiro informado");
        }
        StoredDocumentFile stored = storage.store(file);
        try {
            String documentName = nome == null || nome.isBlank()
                    ? file.getOriginalFilename() : nome.trim();
            if (documentName == null || documentName.isBlank() || documentName.length() > 200) {
                throw new BusinessException("Informe um nome de documento com até 200 caracteres");
            }
            Documento documento = documentoRepository.save(Documento.builder()
                    .parceiro(parceiro)
                    .parceria(parceria)
                    .nome(documentName)
                    .descricao(blankToNull(descricao))
                    .versaoAtual(1)
                    .statusAssinatura("NAO_ENVIADO")
                    .build());
            DocumentoVersao versao = versaoRepository.save(criarVersao(
                    documento, 1, file, responsavelMatricula, stored));
            registrarHistorico(parceiro, "DOCUMENTO",
                    "Documento anexado: " + documentName + " (versão 1).", responsavelMatricula);
            return toDocumentoDTO(documento, versao);
        } catch (RuntimeException exception) {
            storage.delete(stored.storageKey());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentoVersaoDTO> listarVersoes(UUID documentoId) {
        buscarDocumento(documentoId);
        return versaoRepository.findByDocumentoIdOrderByNumeroVersaoDesc(documentoId)
                .stream().map(this::toVersaoDTO).toList();
    }

    @Transactional
    public DocumentoDTO criarVersao(UUID documentoId, String responsavelMatricula, MultipartFile file) {
        validarResponsavel(responsavelMatricula);
        Documento documento = buscarDocumento(documentoId);
        StoredDocumentFile stored = storage.store(file);
        try {
            int numero = documento.getVersaoAtual() + 1;
            DocumentoVersao versao = versaoRepository.save(
                    criarVersao(documento, numero, file, responsavelMatricula, stored));
            documento.setVersaoAtual(numero);
            documento.setStatusAssinatura("NAO_ENVIADO");
            documentoRepository.save(documento);
            registrarHistorico(documento.getParceiro(), "DOCUMENTO",
                    "Nova versão " + numero + " do documento " + documento.getNome() + ".",
                    responsavelMatricula);
            return toDocumentoDTO(documento, versao);
        } catch (RuntimeException exception) {
            storage.delete(stored.storageKey());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public DocumentoDTO buscar(UUID documentoId) {
        Documento documento = buscarDocumento(documentoId);
        DocumentoVersao versao = buscarVersao(documentoId, documento.getVersaoAtual());
        return toDocumentoDTO(documento, versao);
    }

    @Transactional(readOnly = true)
    public DocumentDownload obterArquivo(UUID documentoId, Integer numeroVersao) {
        buscarDocumento(documentoId);
        DocumentoVersao versao = buscarVersao(documentoId, numeroVersao);
        return new DocumentDownload(versao.getNomeArquivo(), versao.getTipoMime(),
                versao.getTamanhoBytes(), storage.load(versao.getChaveArmazenamento()));
    }

    @Transactional(readOnly = true)
    public List<AssinaturaDocumentoDTO> listarAssinaturas(UUID documentoId) {
        buscarDocumento(documentoId);
        return assinaturaRepository.findByDocumentoIdOrderByDataEventoDesc(documentoId)
                .stream().map(this::toAssinaturaDTO).toList();
    }

    @Transactional(noRollbackFor = SignatureIntegrationException.class)
    public AssinaturaDocumentoDTO enviarParaAssinatura(UUID documentoId, String responsavelMatricula) {
        validarResponsavel(responsavelMatricula);
        Documento documento = buscarDocumento(documentoId);
        DocumentoVersao versao = buscarVersao(documentoId, documento.getVersaoAtual());
        if ("AGUARDANDO_ASSINATURA".equals(documento.getStatusAssinatura())
                || "ASSINADO".equals(documento.getStatusAssinatura())) {
            throw new BusinessException("O documento já foi enviado ou assinado nesta versão");
        }

        try {
            SignatureResult retorno = signatureProvider.submit(
                    versao, storage.load(versao.getChaveArmazenamento()), responsavelMatricula.trim());
            validarStatusAssinatura(retorno.status());
            AssinaturaDocumento assinatura = assinaturaRepository.save(AssinaturaDocumento.builder()
                    .documento(documento)
                    .documentoVersao(versao)
                    .provedor(retorno.provider())
                    .status(retorno.status())
                    .protocolo(retorno.protocol())
                    .detalhe(retorno.detail())
                    .responsavelMatricula(responsavelMatricula.trim())
                    .build());
            documento.setStatusAssinatura(retorno.status());
            documentoRepository.save(documento);
            registrarHistorico(documento.getParceiro(), "ASSINATURA",
                    "Documento " + documento.getNome() + " encaminhado para assinatura. Status: "
                            + retorno.status() + ".",
                    responsavelMatricula);
            return toAssinaturaDTO(assinatura);
        } catch (SignatureIntegrationException exception) {
            assinaturaRepository.save(AssinaturaDocumento.builder()
                    .documento(documento)
                    .documentoVersao(versao)
                    .provedor("MOCK")
                    .status("FALHA")
                    .detalhe(exception.getMessage())
                    .responsavelMatricula(responsavelMatricula.trim())
                    .build());
            documento.setStatusAssinatura("FALHA");
            documentoRepository.save(documento);
            registrarHistorico(documento.getParceiro(), "ASSINATURA",
                    "Falha ao encaminhar o documento " + documento.getNome() + " para assinatura.",
                    responsavelMatricula);
            throw exception;
        }
    }

    private Documento buscarDocumento(UUID id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado: " + id));
    }

    private DocumentoVersao buscarVersao(UUID documentoId, Integer numero) {
        return versaoRepository.findByDocumentoIdAndNumeroVersao(documentoId, numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Versão " + numero + " do documento não encontrada"));
    }

    private DocumentoVersao criarVersao(Documento documento, int numero, MultipartFile file,
            String responsavelMatricula, StoredDocumentFile stored) {
        return DocumentoVersao.builder()
                .documento(documento)
                .numeroVersao(numero)
                .nomeArquivo(sanitizeFileName(file.getOriginalFilename()))
                .chaveArmazenamento(stored.storageKey())
                .tipoMime(stored.mimeType())
                .tamanhoBytes(stored.size())
                .sha256(stored.sha256())
                .responsavelMatricula(responsavelMatricula.trim())
                .build();
    }

    private String sanitizeFileName(String fileName) {
        String normalized = fileName == null ? "documento" : fileName.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        String safe = normalized.substring(separator + 1).replaceAll("[\\p{Cntrl}]", "_");
        return safe.length() > 255 ? safe.substring(safe.length() - 255) : safe;
    }

    private void validarResponsavel(String responsavelMatricula) {
        if (responsavelMatricula == null || responsavelMatricula.isBlank()) {
            throw new BusinessException("Informe a matrícula do responsável pela operação");
        }
    }

    private void validarStatusAssinatura(String status) {
        if (!List.of("AGUARDANDO_ASSINATURA", "ASSINADO", "RECUSADO", "CANCELADO").contains(status)) {
            throw new SignatureIntegrationException("O provedor retornou um status de assinatura inválido");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void registrarHistorico(Parceiro parceiro, String tipo, String descricao, String responsavelMatricula) {
        historicoRepository.save(HistoricoParceiro.builder()
                .parceiro(parceiro)
                .tipo(tipo)
                .descricao(descricao)
                .responsavelMatricula(responsavelMatricula.trim())
                .dataEvento(LocalDateTime.now())
                .build());
    }

    private DocumentoDTO toDocumentoDTO(Documento documento) {
        return toDocumentoDTO(documento, buscarVersao(documento.getId(), documento.getVersaoAtual()));
    }

    private DocumentoDTO toDocumentoDTO(Documento documento, DocumentoVersao versao) {
        return new DocumentoDTO(documento.getId(), documento.getParceiro().getId(),
                documento.getParceiro().getRazaoSocial(),
                documento.getParceria() == null ? null : documento.getParceria().getId(),
                documento.getParceria() == null ? null : documento.getParceria().getNome(),
                documento.getNome(), documento.getDescricao(),
                documento.getVersaoAtual(), documento.getStatusAssinatura(), documento.getDataCriacao(),
                toVersaoDTO(versao));
    }

    private DocumentoVersaoDTO toVersaoDTO(DocumentoVersao versao) {
        return new DocumentoVersaoDTO(versao.getId(), versao.getNumeroVersao(), versao.getNomeArquivo(),
                versao.getTipoMime(), versao.getTamanhoBytes(), versao.getSha256(),
                versao.getResponsavelMatricula(), versao.getDataUpload());
    }

    private AssinaturaDocumentoDTO toAssinaturaDTO(AssinaturaDocumento assinatura) {
        return new AssinaturaDocumentoDTO(assinatura.getId(),
                assinatura.getDocumentoVersao().getNumeroVersao(), assinatura.getProvedor(),
                assinatura.getStatus(), assinatura.getProtocolo(), assinatura.getDetalhe(),
                assinatura.getResponsavelMatricula(), assinatura.getDataEvento());
    }

    public record DocumentDownload(String fileName, String mimeType, Long size, Resource resource) {
    }
}
