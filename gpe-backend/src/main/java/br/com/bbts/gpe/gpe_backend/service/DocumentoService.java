package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.*;
import br.com.bbts.gpe.gpe_backend.domain.repository.*;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoDTO;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoVersaoDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {
    private final DocumentoRepository documentoRepository;
    private final DocumentoVersaoRepository versaoRepository;
    private final ParceiroService parceiroService;
    private final OportunidadeRepository oportunidadeRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final ArquivoDocumentoStorageService storage;

    public DocumentoService(DocumentoRepository documentoRepository,
                            DocumentoVersaoRepository versaoRepository,
                            ParceiroService parceiroService,
                            OportunidadeRepository oportunidadeRepository,
                            InstrumentoRepository instrumentoRepository,
                            ArquivoDocumentoStorageService storage) {
        this.documentoRepository = documentoRepository;
        this.versaoRepository = versaoRepository;
        this.parceiroService = parceiroService;
        this.oportunidadeRepository = oportunidadeRepository;
        this.instrumentoRepository = instrumentoRepository;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO> listar(UUID parceiroId, UUID oportunidadeId, UUID instrumentoId) {
        List<Documento> documentos;
        if (parceiroId != null) documentos = documentoRepository.findByParceiroIdAndAtivoTrueOrderByDataAtualizacaoDesc(parceiroId);
        else if (oportunidadeId != null) documentos = documentoRepository.findByOportunidadeIdAndAtivoTrueOrderByDataAtualizacaoDesc(oportunidadeId);
        else if (instrumentoId != null) documentos = documentoRepository.findByInstrumentoIdAndAtivoTrueOrderByDataAtualizacaoDesc(instrumentoId);
        else documentos = documentoRepository.findByAtivoTrueOrderByDataAtualizacaoDesc();
        return documentos.stream().map(this::toDto).toList();
    }

    @Transactional
    public DocumentoDTO criar(String nome, String descricao, UUID parceiroId, UUID oportunidadeId,
                              UUID instrumentoId, MultipartFile arquivo) {
        Documento documento = construirDocumento(nome, descricao, parceiroId, oportunidadeId, instrumentoId);
        documento = documentoRepository.save(documento);
        adicionarVersao(documento, arquivo);
        return toDto(documento);
    }

    @Transactional(readOnly = true)
    public List<DocumentoVersaoDTO> listarVersoes(UUID documentoId) {
        buscarDocumento(documentoId);
        return versaoRepository.findByDocumentoIdOrderByNumeroVersaoDesc(documentoId)
                .stream().map(this::toVersaoDto).toList();
    }

    @Transactional
    public DocumentoVersaoDTO adicionarVersao(UUID documentoId, MultipartFile arquivo) {
        Documento documento = buscarDocumento(documentoId);
        return toVersaoDto(adicionarVersao(documento, arquivo));
    }

    @Transactional(readOnly = true)
    public DownloadFile baixar(UUID documentoId, Integer numeroVersao) {
        DocumentoVersao versao = versaoRepository.findByDocumentoIdAndNumeroVersao(documentoId, numeroVersao)
                .orElseThrow(() -> new ResourceNotFoundException("Versão do documento não encontrada"));
        return new DownloadFile(storage.load(versao.getCaminhoArquivo()), versao.getNomeArquivo(), versao.getContentType());
    }

    private Documento construirDocumento(String nome, String descricao, UUID parceiroId,
                                        UUID oportunidadeId, UUID instrumentoId) {
        if (nome == null || nome.isBlank()) throw new BusinessException("Informe o nome do documento");
        if (parceiroId == null && oportunidadeId == null && instrumentoId == null) {
            throw new BusinessException("Vincule o documento a um parceiro, oportunidade ou instrumento");
        }
        Documento documento = Documento.builder().nome(nome.trim()).descricao(blankToNull(descricao)).ativo(true).build();
        if (parceiroId != null) documento.setParceiro(parceiroService.buscarPorId(parceiroId));
        if (oportunidadeId != null) documento.setOportunidade(oportunidadeRepository.findById(oportunidadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada: " + oportunidadeId)));
        if (instrumentoId != null) documento.setInstrumento(instrumentoRepository.findById(instrumentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Instrumento não encontrado: " + instrumentoId)));
        return documento;
    }

    private DocumentoVersao adicionarVersao(Documento documento, MultipartFile arquivo) {
        int numero = versaoRepository.findTopByDocumentoIdOrderByNumeroVersaoDesc(documento.getId())
                .map(v -> v.getNumeroVersao() + 1).orElse(1);
        ArquivoDocumentoStorageService.StoredFile stored = storage.store(documento.getId(), numero, arquivo);
        DocumentoVersao versao = DocumentoVersao.builder().documento(documento).numeroVersao(numero)
                .nomeArquivo(stored.originalName()).contentType(stored.contentType()).tamanhoBytes(stored.size())
                .caminhoArquivo(stored.path()).hashSha256(stored.sha256()).build();
        documentoRepository.save(documento);
        return versaoRepository.save(versao);
    }

    private Documento buscarDocumento(UUID id) {
        return documentoRepository.findById(id)
                .filter(Documento::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado: " + id));
    }

    private DocumentoDTO toDto(Documento d) {
        DocumentoVersao atual = versaoRepository.findTopByDocumentoIdOrderByNumeroVersaoDesc(d.getId()).orElse(null);
        return new DocumentoDTO(d.getId(), d.getNome(), d.getDescricao(),
                d.getParceiro() == null ? null : d.getParceiro().getId(),
                d.getParceiro() == null ? null : d.getParceiro().getRazaoSocial(),
                d.getOportunidade() == null ? null : d.getOportunidade().getId(),
                d.getOportunidade() == null ? null : d.getOportunidade().getTitulo(),
                d.getInstrumento() == null ? null : d.getInstrumento().getId(),
                d.getInstrumento() == null ? null : d.getInstrumento().getNome(),
                atual == null ? null : atual.getNumeroVersao(),
                atual == null ? null : atual.getNomeArquivo(),
                atual == null ? null : atual.getTamanhoBytes(), d.getDataAtualizacao());
    }

    private DocumentoVersaoDTO toVersaoDto(DocumentoVersao v) {
        return new DocumentoVersaoDTO(v.getId(), v.getNumeroVersao(), v.getNomeArquivo(), v.getContentType(),
                v.getTamanhoBytes(), v.getHashSha256(), v.getDataEnvio());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record DownloadFile(Resource resource, String fileName, String contentType) {}
}