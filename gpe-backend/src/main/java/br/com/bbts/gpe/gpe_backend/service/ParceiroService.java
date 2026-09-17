package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceiroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParceiroService {

    private final ParceiroRepository parceiroRepository;

    public ParceiroService(ParceiroRepository parceiroRepository) {
        this.parceiroRepository = parceiroRepository;
    }

    public List<Parceiro> listarTodos() {
        return parceiroRepository.findAll();
    }

    public Parceiro salvar(Parceiro parceiro) {
        return parceiroRepository.save(parceiro);
    }
}