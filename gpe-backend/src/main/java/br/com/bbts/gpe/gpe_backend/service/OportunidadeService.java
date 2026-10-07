package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
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
    private final ParceiroService parceiroService;

    public OportunidadeService(
            OportunidadeRepository oportunidadeRepository,
            ParceiroService parceiroService) {
        this.oportunidadeRepository = oportunidadeRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada com o ID: " + id));
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
}