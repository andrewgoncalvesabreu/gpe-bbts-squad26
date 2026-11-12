package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceria;
import br.com.bbts.gpe.gpe_backend.domain.model.Projeto;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceriaRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.PlanoTrabalhoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ProjetoRepository;
import br.com.bbts.gpe.gpe_backend.dto.ProjetoDTO;
import br.com.bbts.gpe.gpe_backend.dto.ProjetoRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjetoService {
    private final ProjetoRepository projetoRepository;
    private final ParceriaRepository parceriaRepository;
    private final PlanoTrabalhoRepository planoRepository;

    public ProjetoService(
            ProjetoRepository projetoRepository,
            ParceriaRepository parceriaRepository,
            PlanoTrabalhoRepository planoRepository) {
        this.projetoRepository = projetoRepository;
        this.parceriaRepository = parceriaRepository;
        this.planoRepository = planoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjetoDTO> listar(UUID parceriaId) {
        List<Projeto> projetos;
        if (parceriaId == null) {
            projetos = projetoRepository.findAllByOrderByDataCriacaoDesc();
        } else {
            buscarParceria(parceriaId);
            projetos = projetoRepository.findByParceriaIdOrderByDataCriacaoDesc(parceriaId);
        }
        return projetos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProjetoDTO buscar(UUID id) {
        return toDTO(buscarProjeto(id));
    }

    @Transactional
    public ProjetoDTO criar(ProjetoRequestDTO request) {
        Parceria parceria = buscarParceria(request.parceriaId());
        Projeto projeto = projetoRepository.save(Projeto.builder()
                .parceria(parceria)
                .nome(request.nome().trim())
                .descricao(request.descricao().trim())
                .status("ATIVO")
                .build());
        return toDTO(projeto);
    }

    @Transactional
    public ProjetoDTO atualizar(UUID id, ProjetoRequestDTO request) {
        Projeto projeto = buscarProjeto(id);
        projeto.setParceria(buscarParceria(request.parceriaId()));
        projeto.setNome(request.nome().trim());
        projeto.setDescricao(request.descricao().trim());
        return toDTO(projetoRepository.save(projeto));
    }

    @Transactional
    public void excluir(UUID id) {
        Projeto projeto = buscarProjeto(id);
        projeto.setStatus("CANCELADO");
        projetoRepository.save(projeto);
    }

    private Projeto buscarProjeto(UUID id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado: " + id));
    }

    private Parceria buscarParceria(UUID id) {
        return parceriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parceria não encontrada: " + id));
    }

    private ProjetoDTO toDTO(Projeto projeto) {
        return new ProjetoDTO(projeto.getId(), projeto.getParceria().getId(),
                projeto.getParceria().getNome(), projeto.getParceria().getParceiro().getRazaoSocial(),
                projeto.getNome(), projeto.getDescricao(), projeto.getStatus(), projeto.getDataCriacao(),
                planoRepository.findByProjetoId(projeto.getId()).isPresent());
    }
}
