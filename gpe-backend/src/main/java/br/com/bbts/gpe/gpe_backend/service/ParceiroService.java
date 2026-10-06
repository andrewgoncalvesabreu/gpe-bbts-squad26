package br.com.bbts.gpe.gpe_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceiroRepository;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;

@Service
public class ParceiroService {

    private final ParceiroRepository parceiroRepository;

    public ParceiroService(ParceiroRepository parceiroRepository) {
        this.parceiroRepository = parceiroRepository;
    }

    public List<Parceiro> listarTodos() {
        return parceiroRepository.findAll();
    }

    public Parceiro buscarPorId(UUID id) {
    return parceiroRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Parceiro não encontrado")
            );
}

    public Parceiro salvar(Parceiro parceiro) {

        if (parceiro.getStatus() == null || parceiro.getStatus().isBlank()) {
            parceiro.setStatus("EM_ANALISE");
        }

        if (parceiroRepository.existsByCnpj(parceiro.getCnpj())) {
            throw new BusinessException(
            "Já existe um parceiro cadastrado com este CNPJ"
    );
}

        return parceiroRepository.save(parceiro);
    }

    public Parceiro atualizar(UUID id, Parceiro dados) {

        Parceiro parceiro = buscarPorId(id);

        if (parceiroRepository.existsByCnpjAndIdNot(dados.getCnpj(), id)) {
    throw new BusinessException("Já existe um parceiro cadastrado com este CNPJ");
}

        parceiro.setRazaoSocial(dados.getRazaoSocial());
        parceiro.setNomeFantasia(dados.getNomeFantasia());
        parceiro.setCnpj(dados.getCnpj());
        parceiro.setTipoOrganizacao(dados.getTipoOrganizacao());
        parceiro.setSegmentoAtuacao(dados.getSegmentoAtuacao());
        parceiro.setPorte(dados.getPorte());
        parceiro.setEmailContato(dados.getEmailContato());
        parceiro.setTelefone(dados.getTelefone());
        parceiro.setEnderecoCompleto(dados.getEnderecoCompleto());
        parceiro.setResponsaveis(dados.getResponsaveis());

        if (dados.getStatus() != null && !dados.getStatus().isBlank()) {
            parceiro.setStatus(dados.getStatus());
        }

        return parceiroRepository.save(parceiro);
    }

    // Exclusão lógica: preserva histórico e vínculos (oportunidades, instrumentos etc.)
public void excluir(UUID id) {
    Parceiro parceiro = buscarPorId(id);
    parceiro.setStatus("INATIVO");
    parceiroRepository.save(parceiro);
}
}