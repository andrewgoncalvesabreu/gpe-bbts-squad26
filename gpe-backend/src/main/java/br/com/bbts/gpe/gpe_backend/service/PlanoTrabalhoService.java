package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.*;
import br.com.bbts.gpe.gpe_backend.domain.repository.PlanoTrabalhoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ProjetoRepository;
import br.com.bbts.gpe.gpe_backend.dto.PlanoTrabalhoDTO;
import br.com.bbts.gpe.gpe_backend.dto.PlanoTrabalhoRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PlanoTrabalhoService {
    private final PlanoTrabalhoRepository planoRepository;
    private final ProjetoRepository projetoRepository;

    public PlanoTrabalhoService(PlanoTrabalhoRepository planoRepository, ProjetoRepository projetoRepository) {
        this.planoRepository = planoRepository;
        this.projetoRepository = projetoRepository;
    }

    @Transactional(readOnly = true)
    public PlanoTrabalhoDTO buscar(UUID projetoId) {
        return toDTO(buscarPlano(projetoId));
    }

    @Transactional
    public PlanoTrabalhoDTO criar(UUID projetoId, PlanoTrabalhoRequestDTO request) {
        Projeto projeto = buscarProjeto(projetoId);
        if (planoRepository.findByProjetoId(projetoId).isPresent()) {
            throw new BusinessException("O projeto já possui um plano de trabalho");
        }
        validarDatas(request);
        PlanoTrabalho plano = PlanoTrabalho.builder()
                .projeto(projeto)
                .cronogramaInicio(request.cronogramaInicio())
                .cronogramaFim(request.cronogramaFim())
                .responsaveis(request.responsaveis().trim())
                .build();
        preencherItens(plano, request);
        return toDTO(planoRepository.save(plano));
    }

    @Transactional
    public PlanoTrabalhoDTO atualizar(UUID projetoId, PlanoTrabalhoRequestDTO request) {
        PlanoTrabalho plano = buscarPlano(projetoId);
        validarDatas(request);
        plano.setCronogramaInicio(request.cronogramaInicio());
        plano.setCronogramaFim(request.cronogramaFim());
        plano.setResponsaveis(request.responsaveis().trim());
        plano.getObjetivos().clear();
        plano.getEntregas().clear();
        plano.getMarcos().clear();
        preencherItens(plano, request);
        return toDTO(planoRepository.save(plano));
    }

    @Transactional
    public void excluir(UUID projetoId) {
        planoRepository.delete(buscarPlano(projetoId));
    }

    private void validarDatas(PlanoTrabalhoRequestDTO request) {
        LocalDate inicio = request.cronogramaInicio();
        LocalDate fim = request.cronogramaFim();
        if (fim.isBefore(inicio)) {
            throw new BusinessException("A data final do cronograma não pode ser anterior à data inicial");
        }
        if (request.entregas() != null && request.entregas().stream()
                .anyMatch(item -> item.dataPrevista().isBefore(inicio) || item.dataPrevista().isAfter(fim))) {
            throw new BusinessException("As datas das entregas devem estar dentro do cronograma do plano");
        }
        if (request.marcos() != null && request.marcos().stream()
                .anyMatch(item -> item.dataPrevista().isBefore(inicio) || item.dataPrevista().isAfter(fim))) {
            throw new BusinessException("As datas dos marcos devem estar dentro do cronograma do plano");
        }
    }

    private void preencherItens(PlanoTrabalho plano, PlanoTrabalhoRequestDTO request) {
        request.objetivos().stream().map(String::trim).filter(value -> !value.isEmpty()).forEach(descricao ->
                plano.getObjetivos().add(PlanoObjetivo.builder().planoTrabalho(plano).descricao(descricao).build()));
        if (request.entregas() != null) {
            request.entregas().forEach(item -> plano.getEntregas().add(PlanoEntrega.builder()
                    .planoTrabalho(plano)
                    .descricao(item.descricao().trim())
                    .dataPrevista(item.dataPrevista())
                    .responsavel(item.responsavel().trim())
                    .build()));
        }
        if (request.marcos() != null) {
            request.marcos().forEach(item -> plano.getMarcos().add(PlanoMarco.builder()
                    .planoTrabalho(plano)
                    .descricao(item.descricao().trim())
                    .dataPrevista(item.dataPrevista())
                    .build()));
        }
    }

    private Projeto buscarProjeto(UUID id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado: " + id));
    }

    private PlanoTrabalho buscarPlano(UUID projetoId) {
        buscarProjeto(projetoId);
        return planoRepository.findByProjetoId(projetoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plano de trabalho não encontrado para o projeto: " + projetoId));
    }

    private PlanoTrabalhoDTO toDTO(PlanoTrabalho plano) {
        return new PlanoTrabalhoDTO(plano.getId(), plano.getProjeto().getId(),
                plano.getObjetivos().stream().map(PlanoObjetivo::getDescricao).toList(),
                plano.getCronogramaInicio(), plano.getCronogramaFim(), plano.getResponsaveis(),
                plano.getEntregas().stream().map(item -> new PlanoTrabalhoDTO.EntregaDTO(
                        item.getId(), item.getDescricao(), item.getDataPrevista(), item.getResponsavel())).toList(),
                plano.getMarcos().stream().map(item -> new PlanoTrabalhoDTO.MarcoDTO(
                        item.getId(), item.getDescricao(), item.getDataPrevista())).toList(),
                plano.getDataCriacao());
    }
}
