package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.AvaliacaoOportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.AvaliacaoOportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.dto.AvaliacaoRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OportunidadeService {

    private static final String STATUS_INICIAL = "TRIAGEM";
    private static final String STATUS_CANCELADA = "CANCELADA";

    private final OportunidadeRepository oportunidadeRepository;
    private final AvaliacaoOportunidadeRepository avaliacaoOportunidadeRepository;
    private final ParceiroService parceiroService;

    public OportunidadeService(
            OportunidadeRepository oportunidadeRepository,
            AvaliacaoOportunidadeRepository avaliacaoOportunidadeRepository,
            ParceiroService parceiroService) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.avaliacaoOportunidadeRepository = avaliacaoOportunidadeRepository;
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

        return oportunidadeRepository.save(oportunidade);
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

        return oportunidadeRepository.save(oportunidade);
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

        AvaliacaoOportunidade avaliacao = AvaliacaoOportunidade.builder()
                .oportunidade(oportunidade)
                .alinhamentoEstrategico(dto.getAlinhamentoEstrategico())
                .potencialInovacao(dto.getPotencialInovacao())
                .retornoFinanceiro(dto.getRetornoFinanceiro())
                .impactoInstitucional(dto.getImpactoInstitucional())
                .viabilidadeTecnica(dto.getViabilidadeTecnica())
                .parecerTecnico(dto.getParecerTecnico())
                .avaliadorMatricula(dto.getAvaliadorMatricula())
                .build();

        // Se estava em TRIAGEM, avança para EM_AVALIACAO ao receber nota
        if (STATUS_INICIAL.equalsIgnoreCase(oportunidade.getStatusWorkflow())) {
            oportunidade.setStatusWorkflow("EM_AVALIACAO");
            oportunidadeRepository.save(oportunidade);
        }

        return avaliacaoOportunidadeRepository.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoOportunidade> listarAvaliacoes(UUID oportunidadeId) {
        buscarPorId(oportunidadeId);
        return avaliacaoOportunidadeRepository.findByOportunidadeIdOrderByDataAvaliacaoDesc(oportunidadeId);
    }

    // --- RF006: Fluxo de Aprovação ---
    @Transactional
    public Oportunidade alterarStatusWorkflow(UUID id, String novoStatus) {
        Oportunidade oportunidade = buscarPorId(id);
        
        String statusUpper = novoStatus.toUpperCase();
        List<String> statusValidos = List.of("TRIAGEM", "EM_AVALIACAO", "APROVADA", "REJEITADA", "CANCELADA");
        
        if (!statusValidos.contains(statusUpper)) {
            throw new BusinessException("Status inválido. Permitidos: " + statusValidos);
        }

        oportunidade.setStatusWorkflow(statusUpper);
        return oportunidadeRepository.save(oportunidade);
    }
}