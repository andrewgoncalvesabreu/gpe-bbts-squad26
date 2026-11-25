package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.AssinaturaDocumento;
import br.com.bbts.gpe.gpe_backend.domain.model.Documento;
import br.com.bbts.gpe.gpe_backend.domain.model.DocumentoVersao;
import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceria;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.AssinaturaDocumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.DocumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.DocumentoVersaoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentoService {
    private final DocumentoRepository documentoRepository;
    private final DocumentoVersaoRepository versaoRepository;
    private final AssinaturaDocumentoRepository assinaturaRepository;
    private final HistoricoParceiroRepository historicoRepository;
    private final ParceiroService parceiroService;
    private final OportunidadeRepository oportunidadeRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final ParceriaRepository parceriaRepository;
    private final ArquivoDocumentoStorageService storage;
    private final DocumentSignatureProvider signatureProvider;
    private final AuditoriaService auditoriaService;

    public DocumentoService(
            DocumentoRepository documentoRepository,
            DocumentoVersaoRepository versaoRepository,
            AssinaturaDocumentoRepository assinaturaRepository,
            HistoricoParceiroRepository historicoRepository,
            ParceiroService parceiroService,
            OportunidadeRepository oportunidadeRepository,
            InstrumentoRepository instrumentoRepository,
            ParceriaRepository parceriaRepository,
            ArquivoDocumentoStorageService storage,
            DocumentSignatureProvider signatureProvider,
            AuditoriaService auditoriaService) {
        this.documentoRepository = documentoRepository;
        this.versaoRepository = versaoRepository;
        this.assinaturaRepository = assinaturaRepository;
        this.historicoRepository = historicoRepository;
        this.parceiroService = parceiroService;
        this.oportunidadeRepository = oportunidadeRepository;
        this.instrumentoRepository = instrumentoRepository;
        this.parceriaRepository = parceriaRepository;
        this.storage = storage;
        this.signatureProvider = signatureProvider;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO> listar(
            UUID parceiroId, UUID oportunidadeId, UUID instrumentoId, UUID parceriaId) {
        List<Documento> documentos;
        if (parceiroId != null) {
            documentos = documentoRepository.findByParceiroIdAndAtivoTrueOrderByDataAtualizacaoDesc(parceiroId);
        } else if (oportunidadeId != null) {
            documentos = documentoRepository.findByOportunidadeIdAndAtivoTrueOrderByDataAtualizacaoDesc(oportunidadeId);
        } else if (instrumentoId != null) {
            documentos = documentoRepository.findByInstrumentoIdAndAtivoTrueOrderByDataAtualizacaoDesc(instrumentoId);
        } else if (parceriaId != null) {
            documentos = documentoRepository.findByParceriaIdAndAtivoTrueOrderByDataAtualizacaoDesc(parceriaId);
        } else {
            documentos = documentoRepository.findByAtivoTrueOrderByDataAtualizacaoDesc();
        }
        return documentos.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarPorParceiro(UUID parceiroId) {
        parceiroService.buscarPorId(parceiroId);
        return listar(parceiroId, null, null, null);
    }

    @Transactional
    public DocumentoDTO criar(
            String nome,
            String descricao,
            UUID parceiroId,
            UUID oportunidadeId,
            UUID instrumentoId,
            UUID parceriaId,
            String responsavelMatricula,
            MultipartFile arquivo) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Informe o nome do documento");
        }
        if (parceiroId == null && oportunidadeId == null && instrumentoId == null && parceriaId == null) {
            throw new BusinessException("Vincule o documento a um parceiro, parceria, oportunidade ou instrumento");
        }
        if (responsavelMatricula != null && !responsavelMatricula.isBlank()) {
            validarResponsavel(responsavelMatricula);
        }

        Documento documento = Documento.builder()
                .nome(nome.trim())
                .descricao(blankToNull(descricao))
                .ativo(true)
                .versaoAtual(1)
                .statusAssinatura("NAO_ENVIADO")
                .build();
        if (parceiroId != null) {
            documento.setParceiro(parceiroService.buscarPorId(parceiroId));
        }
        if (oportunidadeId != null) {
            Oportunidade oportunidade = oportunidadeRepository.findById(oportunidadeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada: " + oportunidadeId));
            documento.setOportunidade(oportunidade);
            if (documento.getParceiro() != null
                    && !documento.getParceiro().getId().equals(oportunidade.getParceiro().getId())) {
                throw new BusinessException("A oportunidade deve pertencer ao parceiro selecionado");
            }
        }
        if (instrumentoId != null) {
            InstrumentoJuridico instrumento = instrumentoRepository.findById(instrumentoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Instrumento não encontrado: " + instrumentoId));
            documento.setInstrumento(instrumento);
            if (documento.getParceiro() != null
                    && !documento.getParceiro().getId().equals(instrumento.getParceiro().getId())) {
                throw new BusinessException("O instrumento deve pertencer ao parceiro selecionado");
            }
        }
        if (parceriaId != null) {
            Parceria parceria = parceriaRepository.findById(parceriaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parceria não encontrada: " + parceriaId));
            if (documento.getParceiro() != null
                    && !documento.getParceiro().getId().equals(parceria.getParceiro().getId())) {
                throw new BusinessException("A parceria selecionada não pertence ao parceiro informado");
            }
            if (documento.getParceiro() == null) {
                documento.setParceiro(parceria.getParceiro());
            }
            documento.setParceria(parceria);
        }

        documento = documentoRepository.save(documento);
        adicionarVersao(documento, arquivo, responsavelMatricula);
        auditoriaService.registrar("CREATE", "Documento", documento.getId(), null, snapshot(documento), null);
        return toDto(documento);
    }

    @Transactional
    public DocumentoDTO criarParaParceria(
            UUID parceiroId,
            UUID parceriaId,
            String nome,
            String descricao,
            String responsavelMatricula,
            MultipartFile arquivo) {
        validarResponsavel(responsavelMatricula);
        return criar(nome, descricao, parceiroId, null, null, parceriaId, responsavelMatricula, arquivo);
    }

    @Transactional(readOnly = true)
    public List<DocumentoVersaoDTO> listarVersoes(UUID documentoId) {
        buscarDocumento(documentoId);
        return versaoRepository.findByDocumentoIdOrderByNumeroVersaoDesc(documentoId)
                .stream().map(this::toVersaoDto).toList();
    }

    @Transactional
    public DocumentoVersaoDTO adicionarVersao(
            UUID documentoId, MultipartFile arquivo, String responsavelMatricula) {
        if (responsavelMatricula != null && !responsavelMatricula.isBlank()) {
            validarResponsavel(responsavelMatricula);
        }
        Documento documento = buscarDocumento(documentoId);
        DocumentoVersao versao = adicionarVersao(documento, arquivo, responsavelMatricula);
        registrarHistorico(documento, "DOCUMENTO",
                "Nova versão " + versao.getNumeroVersao() + " do documento " + documento.getNome() + ".",
                responsavelMatricula);
        auditoriaService.registrar("ADD_VERSION", "Documento", documento.getId(),
                snapshot(documento, versao.getNumeroVersao() - 1), snapshot(documento), null);
        return toVersaoDto(versao);
    }

    @Transactional(readOnly = true)
    public DocumentoDTO buscar(UUID documentoId) {
        return toDto(buscarDocumento(documentoId));
    }

    @Transactional
    public DownloadFile baixar(UUID documentoId, Integer numeroVersao) {
        buscarDocumento(documentoId);
        DocumentoVersao versao = buscarVersao(documentoId, numeroVersao);
        DownloadFile arquivo = new DownloadFile(
                storage.load(versao.getCaminhoArquivo()),
                versao.getNomeArquivo(),
                versao.getContentType(),
                versao.getTamanhoBytes());
        auditoriaService.registrar("DOWNLOAD", "Documento", documentoId, null,
                snapshotDownload(versao), null);
        return arquivo;
    }

    @Transactional(readOnly = true)
    public DownloadFile baixarAtual(UUID documentoId) {
        Documento documento = buscarDocumento(documentoId);
        return baixar(documentoId, documento.getVersaoAtual());
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
                    versao, storage.load(versao.getCaminhoArquivo()), responsavelMatricula.trim());
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
            registrarHistorico(documento, "ASSINATURA",
                    "Documento " + documento.getNome() + " encaminhado para assinatura. Status: "
                            + retorno.status() + ".",
                    responsavelMatricula);
            auditoriaService.registrar("SUBMIT_FOR_SIGNATURE", "Documento", documento.getId(),
                    null, snapshotSignature(documento, assinatura), null);
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
            registrarHistorico(documento, "ASSINATURA",
                    "Falha ao encaminhar o documento " + documento.getNome() + " para assinatura.",
                    responsavelMatricula);
            auditoriaService.registrar("SIGNATURE_FAILURE", "Documento", documento.getId(),
                    null, snapshot(documento), null);
            throw exception;
        }
    }

    private DocumentoVersao adicionarVersao(
            Documento documento, MultipartFile arquivo, String responsavelMatricula) {
        int numero = versaoRepository.findTopByDocumentoIdOrderByNumeroVersaoDesc(documento.getId())
                .map(versao -> versao.getNumeroVersao() + 1)
                .orElse(1);
        ArquivoDocumentoStorageService.StoredFile stored = storage.store(documento.getId(), numero, arquivo);
        DocumentoVersao versao = versaoRepository.save(DocumentoVersao.builder()
                .documento(documento)
                .numeroVersao(numero)
                .nomeArquivo(stored.originalName())
                .contentType(stored.contentType())
                .tamanhoBytes(stored.size())
                .caminhoArquivo(stored.path())
                .hashSha256(stored.sha256())
                .responsavelMatricula(blankToNull(responsavelMatricula))
                .build());
        documento.setVersaoAtual(numero);
        documento.setStatusAssinatura("NAO_ENVIADO");
        documentoRepository.save(documento);
        return versao;
    }

    private Documento buscarDocumento(UUID id) {
        return documentoRepository.findById(id)
                .filter(Documento::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado: " + id));
    }

    private DocumentoVersao buscarVersao(UUID documentoId, Integer numero) {
        return versaoRepository.findByDocumentoIdAndNumeroVersao(documentoId, numero)
                .orElseThrow(() -> new ResourceNotFoundException("Versão " + numero + " do documento não encontrada"));
    }

    private DocumentoDTO toDto(Documento documento) {
        DocumentoVersao atual = versaoRepository.findByDocumentoIdAndNumeroVersao(
                documento.getId(), documento.getVersaoAtual()).orElse(null);
        DocumentoVersaoDTO versaoDTO = atual == null ? null : toVersaoDto(atual);
        return new DocumentoDTO(
                documento.getId(),
                documento.getNome(),
                documento.getDescricao(),
                documento.getParceiro() == null ? null : documento.getParceiro().getId(),
                documento.getParceiro() == null ? null : documento.getParceiro().getRazaoSocial(),
                documento.getOportunidade() == null ? null : documento.getOportunidade().getId(),
                documento.getOportunidade() == null ? null : documento.getOportunidade().getTitulo(),
                documento.getInstrumento() == null ? null : documento.getInstrumento().getId(),
                documento.getInstrumento() == null ? null : documento.getInstrumento().getNome(),
                documento.getParceria() == null ? null : documento.getParceria().getId(),
                documento.getParceria() == null ? null : documento.getParceria().getNome(),
                documento.getVersaoAtual(),
                atual == null ? null : atual.getNomeArquivo(),
                atual == null ? null : atual.getTamanhoBytes(),
                documento.getStatusAssinatura(),
                documento.getDataCriacao(),
                documento.getDataAtualizacao(),
                versaoDTO);
    }

    private DocumentoVersaoDTO toVersaoDto(DocumentoVersao versao) {
        return new DocumentoVersaoDTO(
                versao.getId(),
                versao.getNumeroVersao(),
                versao.getNomeArquivo(),
                versao.getContentType(),
                versao.getTamanhoBytes(),
                versao.getHashSha256(),
                versao.getResponsavelMatricula(),
                versao.getDataEnvio());
    }

    private AssinaturaDocumentoDTO toAssinaturaDTO(AssinaturaDocumento assinatura) {
        return new AssinaturaDocumentoDTO(
                assinatura.getId(),
                assinatura.getDocumentoVersao().getNumeroVersao(),
                assinatura.getProvedor(),
                assinatura.getStatus(),
                assinatura.getProtocolo(),
                assinatura.getDetalhe(),
                assinatura.getResponsavelMatricula(),
                assinatura.getDataEvento());
    }

    private void registrarHistorico(
            Documento documento, String tipo, String descricao, String responsavelMatricula) {
        if (responsavelMatricula == null || responsavelMatricula.isBlank()) {
            return;
        }
        Parceiro parceiro = documento.getParceiro();
        if (parceiro == null && documento.getParceria() != null) {
            parceiro = documento.getParceria().getParceiro();
        }
        if (parceiro == null && documento.getOportunidade() != null) {
            parceiro = documento.getOportunidade().getParceiro();
        }
        if (parceiro == null && documento.getInstrumento() != null) {
            parceiro = documento.getInstrumento().getParceiro();
        }
        if (parceiro != null) {
            historicoRepository.save(HistoricoParceiro.builder()
                    .parceiro(parceiro)
                    .tipo(tipo)
                    .descricao(descricao)
                    .responsavelMatricula(responsavelMatricula.trim())
                    .dataEvento(LocalDateTime.now())
                    .build());
        }
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

    private Map<String, Object> snapshot(Documento documento) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", documento.getId());
        snapshot.put("nome", documento.getNome());
        snapshot.put("versaoAtual", documento.getVersaoAtual());
        snapshot.put("statusAssinatura", documento.getStatusAssinatura());
        snapshot.put("parceiroId", documento.getParceiro() == null ? null : documento.getParceiro().getId());
        snapshot.put("oportunidadeId", documento.getOportunidade() == null ? null : documento.getOportunidade().getId());
        snapshot.put("instrumentoId", documento.getInstrumento() == null ? null : documento.getInstrumento().getId());
        snapshot.put("parceriaId", documento.getParceria() == null ? null : documento.getParceria().getId());
        return snapshot;
    }

    private Map<String, Object> snapshot(Documento documento, int versaoAnterior) {
        Map<String, Object> snapshot = snapshot(documento);
        snapshot.put("versaoAnterior", versaoAnterior);
        return snapshot;
    }

    private Map<String, Object> snapshotDownload(DocumentoVersao versao) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("versao", versao.getNumeroVersao());
        snapshot.put("nomeArquivo", versao.getNomeArquivo());
        snapshot.put("tamanhoBytes", versao.getTamanhoBytes());
        snapshot.put("hashSha256", versao.getHashSha256());
        return snapshot;
    }

    private Map<String, Object> snapshotSignature(Documento documento, AssinaturaDocumento assinatura) {
        Map<String, Object> snapshot = snapshot(documento);
        snapshot.put("provedor", assinatura.getProvedor());
        snapshot.put("status", assinatura.getStatus());
        snapshot.put("protocolo", assinatura.getProtocolo());
        snapshot.put("versao", assinatura.getDocumentoVersao().getNumeroVersao());
        return snapshot;
    }

    public record DownloadFile(Resource resource, String fileName, String contentType, Long size) {
    }
}
