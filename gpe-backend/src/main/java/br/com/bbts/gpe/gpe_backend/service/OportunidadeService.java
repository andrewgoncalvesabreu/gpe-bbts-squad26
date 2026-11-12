package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.AvaliacaoOportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.AvaliacaoOportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.dto.AvaliacaoRequestDTO;
import br.com.bbts.gpe.gpe_backend.dto.WorkflowDecisionDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class OportunidadeService {

    private static final String STATUS_INICIAL = "TRIAGEM";
    private static final String STATUS_EM_AVALIACAO = "EM_AVALIACAO";
    private static final String STATUS_EM_APROVACAO = "EM_APROVACAO";
    private static final String STATUS_CANCELADA = "CANCELADA";

    private final OportunidadeRepository oportunidadeRepository;
    private final AvaliacaoOportunidadeRepository avaliacaoOportunidadeRepository;
    private final HistoricoParceiroRepository historicoParceiroRepository;
    private final ParceiroService parceiroService;

    public OportunidadeService(
            OportunidadeRepository oportunidadeRepository,
            AvaliacaoOportunidadeRepository avaliacaoOportunidadeRepository,
            HistoricoParceiroRepository historicoParceiroRepository,
            ParceiroService parceiroService) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.avaliacaoOportunidadeRepository = avaliacaoOportunidadeRepository;
        this.historicoParceiroRepository = historicoParceiroRepository;
        this.parceiroService = parceiroService;
    }

    @Transactional(readOnly = true)
    public List<Oportunidade> listar(UUID parceiroId) {
        if (parceiroId != null) {
            parceiroService.buscarPorId(parceiroId);
            return oportunidadeRepository.findByParceiroIdOrderByDataCriacaoDesc(parceiroId);
        }
        return oportunidadeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Oportunidade buscarPorId(UUID id) {
        return oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada: " + id));
    }

    @Transactional
    public Oportunidade salvar(Oportunidade oportunidade) {
        if (oportunidade.getParceiro() == null || oportunidade.getParceiro().getId() == null) {
            throw new BusinessException("Informe o parceiro da oportunidade (parceiro.id)");
        }

        Parceiro parceiro = parceiroService.buscarPorId(oportunidade.getParceiro().getId());

        if ("INATIVO".equalsIgnoreCase(parceiro.getStatus())) {
            throw new BusinessException("Não é possível registrar oportunidade para parceiro inativo");
        }

        oportunidade.setId(null);
        oportunidade.setParceiro(parceiro);
        oportunidade.setStatusWorkflow(STATUS_INICIAL);
        oportunidade.setResponsavelMatricula(oportunidade.getResponsavelMatricula().trim());

        Oportunidade salva = oportunidadeRepository.save(oportunidade);
        registrarHistorico(parceiro, "OPORTUNIDADE",
                "Oportunidade cadastrada: " + salva.getTitulo() + ".",
                salva.getResponsavelMatricula());
        return salva;
    }

    @Transactional
    public Oportunidade atualizar(UUID id, Oportunidade dados) {
        Oportunidade oportunidade = buscarPorId(id);

        oportunidade.setTitulo(dados.getTitulo());
        oportunidade.setOrigem(dados.getOrigem());
        oportunidade.setDescricao(dados.getDescricao());
        oportunidade.setObjetivos(dados.getObjetivos());
        oportunidade.setJustificativa(dados.getJustificativa());
        oportunidade.setBeneficiosEsperados(dados.getBeneficiosEsperados());
        oportunidade.setProblemaDemandado(dados.getProblemaDemandado());
        oportunidade.setSolucaoProposta(dados.getSolucaoProposta());
        oportunidade.setEstimativaFinanceira(dados.getEstimativaFinanceira());
        oportunidade.setAreaDemandante(dados.getAreaDemandante());
        oportunidade.setResponsavelMatricula(dados.getResponsavelMatricula());

        Oportunidade salva = oportunidadeRepository.save(oportunidade);
        registrarHistorico(salva.getParceiro(), "OPORTUNIDADE",
                "Oportunidade atualizada: " + salva.getTitulo() + ".",
                salva.getResponsavelMatricula());
        return salva;
    }

    @Transactional
    public void excluir(UUID id) {
        Oportunidade oportunidade = buscarPorId(id);
        oportunidade.setStatusWorkflow(STATUS_CANCELADA);
        oportunidadeRepository.save(oportunidade);
    }

    // --- RF005: Avaliação de Oportunidades ---
    @Transactional
    public AvaliacaoOportunidade avaliar(UUID oportunidadeId, AvaliacaoRequestDTO dto) {
        Oportunidade oportunidade = buscarPorId(oportunidadeId);
        if (!STATUS_INICIAL.equalsIgnoreCase(oportunidade.getStatusWorkflow())
                && !STATUS_EM_AVALIACAO.equalsIgnoreCase(oportunidade.getStatusWorkflow())) {
            throw new BusinessException("A oportunidade não está disponível para avaliação");
        }

        AvaliacaoOportunidade avaliacao = AvaliacaoOportunidade.builder()
                .oportunidade(oportunidade)
                .alinhamentoEstrategico(dto.getAlinhamentoEstrategico())
                .potencialInovacao(dto.getPotencialInovacao())
                .retornoFinanceiro(dto.getRetornoFinanceiro())
                .impactoInstitucional(dto.getImpactoInstitucional())
                .viabilidadeTecnica(dto.getViabilidadeTecnica())
                .parecerTecnico(dto.getParecerTecnico())
                .avaliadorMatricula(dto.getAvaliadorMatricula().trim())
                .build();

        // Se estava em TRIAGEM, avança para EM_AVALIACAO ao receber nota
        if (STATUS_INICIAL.equalsIgnoreCase(oportunidade.getStatusWorkflow())) {
            oportunidade.setStatusWorkflow(STATUS_EM_AVALIACAO);
            oportunidadeRepository.save(oportunidade);
        }

        AvaliacaoOportunidade salva = avaliacaoOportunidadeRepository.save(avaliacao);
        registrarHistorico(oportunidade.getParceiro(), "AVALIACAO_OPORTUNIDADE",
                "Avaliação registrada para a oportunidade: " + oportunidade.getTitulo() + ".",
                dto.getAvaliadorMatricula());
        return salva;
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoOportunidade> listarAvaliacoes(UUID oportunidadeId) {
        buscarPorId(oportunidadeId);
        return avaliacaoOportunidadeRepository.findByOportunidadeIdOrderByDataAvaliacaoDesc(oportunidadeId);
    }

    @Transactional
    public Oportunidade enviarParaAprovacao(UUID id, WorkflowDecisionDTO dto) {
        Oportunidade oportunidade = buscarPorId(id);
        if (!STATUS_EM_AVALIACAO.equalsIgnoreCase(oportunidade.getStatusWorkflow())
                || !avaliacaoOportunidadeRepository.existsByOportunidadeId(id)) {
            throw new BusinessException("A oportunidade precisa estar avaliada antes do envio para aprovação");
        }
        oportunidade.setStatusWorkflow(STATUS_EM_APROVACAO);
        Oportunidade salva = oportunidadeRepository.save(oportunidade);
        registrarHistorico(oportunidade.getParceiro(), "FLUXO_APROVACAO",
                "Oportunidade enviada para aprovação.", dto.getResponsavelMatricula());
        return salva;
    }

    @Transactional
    public Oportunidade aprovar(UUID id, WorkflowDecisionDTO dto) {
        return decidir(id, dto, "APROVADA", "Oportunidade aprovada.");
    }

    @Transactional
    public Oportunidade reprovar(UUID id, WorkflowDecisionDTO dto) {
        if (dto.getJustificativa() == null || dto.getJustificativa().isBlank()) {
            throw new BusinessException("Informe a justificativa da reprovação");
        }
        return decidir(id, dto, "REJEITADA",
                "Oportunidade reprovada. Justificativa: " + dto.getJustificativa().trim());
    }

    private Oportunidade decidir(UUID id, WorkflowDecisionDTO dto, String status, String descricao) {
        Oportunidade oportunidade = buscarPorId(id);
        if (!STATUS_EM_APROVACAO.equalsIgnoreCase(oportunidade.getStatusWorkflow())) {
            throw new BusinessException("A oportunidade não está aguardando aprovação");
        }
        oportunidade.setStatusWorkflow(status);
        Oportunidade salva = oportunidadeRepository.save(oportunidade);
        registrarHistorico(oportunidade.getParceiro(), "FLUXO_APROVACAO", descricao, dto.getResponsavelMatricula());
        return salva;
    }

    private void registrarHistorico(Parceiro parceiro, String tipo, String descricao, String responsavelMatricula) {
        historicoParceiroRepository.save(HistoricoParceiro.builder()
                .parceiro(parceiro)
                .tipo(tipo)
                .descricao(descricao)
                .responsavelMatricula(responsavelMatricula.trim())
                .dataEvento(LocalDateTime.now())
                .build());
    }
}