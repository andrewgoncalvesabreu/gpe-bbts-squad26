package br.com.bbts.gpe.gpe_backend.service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceiroRepository;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParceiroService {

    private final ParceiroRepository parceiroRepository;
    private final HistoricoParceiroRepository historicoRepository;

    public ParceiroService(
            ParceiroRepository parceiroRepository,
            HistoricoParceiroRepository historicoRepository) {
        this.parceiroRepository = parceiroRepository;
        this.historicoRepository = historicoRepository;
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

    @Transactional
    public Parceiro salvar(Parceiro parceiro, String responsavelMatricula) {
        validarResponsavel(responsavelMatricula);

        if (parceiro.getStatus() == null || parceiro.getStatus().isBlank()) {
            parceiro.setStatus("EM_ANALISE");
        }

        if (parceiroRepository.existsByCnpj(parceiro.getCnpj())) {
            throw new BusinessException(
            "Já existe um parceiro cadastrado com este CNPJ"
    );
}

        Parceiro salvo = parceiroRepository.save(parceiro);
        registrarHistorico(salvo, "ALTERACAO_CADASTRAL", "Cadastro inicial do parceiro.", responsavelMatricula);
        return salvo;
    }

    @Transactional
    public Parceiro atualizar(UUID id, Parceiro dados, String responsavelMatricula) {
        validarResponsavel(responsavelMatricula);

        Parceiro parceiro = buscarPorId(id);

        if (parceiroRepository.existsByCnpjAndIdNot(dados.getCnpj(), id)) {
    throw new BusinessException("Já existe um parceiro cadastrado com este CNPJ");
}

        List<String> camposAlterados = new java.util.ArrayList<>();
        compararAlteracao(camposAlterados, "Razão Social", parceiro.getRazaoSocial(), dados.getRazaoSocial());
        compararAlteracao(camposAlterados, "Nome Fantasia", parceiro.getNomeFantasia(), dados.getNomeFantasia());
        compararAlteracao(camposAlterados, "CNPJ", parceiro.getCnpj(), dados.getCnpj());
        compararAlteracao(camposAlterados, "Tipo de Organização", parceiro.getTipoOrganizacao(), dados.getTipoOrganizacao());
        compararAlteracao(camposAlterados, "Segmento de Atuação", parceiro.getSegmentoAtuacao(), dados.getSegmentoAtuacao());
        compararAlteracao(camposAlterados, "Porte", parceiro.getPorte(), dados.getPorte());
        compararAlteracao(camposAlterados, "E-mail", parceiro.getEmailContato(), dados.getEmailContato());
        compararAlteracao(camposAlterados, "Telefone", parceiro.getTelefone(), dados.getTelefone());
        compararAlteracao(camposAlterados, "Endereço", parceiro.getEnderecoCompleto(), dados.getEnderecoCompleto());
        compararAlteracao(camposAlterados, "Responsáveis", parceiro.getResponsaveis(), dados.getResponsaveis());
        if (dados.getStatus() != null && !dados.getStatus().isBlank()) {
            compararAlteracao(camposAlterados, "Status", parceiro.getStatus(), dados.getStatus());
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

        Parceiro salvo = parceiroRepository.save(parceiro);
        if (!camposAlterados.isEmpty()) {
            registrarHistorico(salvo, "ALTERACAO_CADASTRAL",
                    "Campos alterados: " + String.join(", ", camposAlterados) + ".",
                    responsavelMatricula);
        }
        return salvo;
    }

    private void validarResponsavel(String responsavelMatricula) {
        if (responsavelMatricula == null || responsavelMatricula.isBlank()) {
            throw new BusinessException("Informe a matrícula do responsável pela operação");
        }
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

    private void compararAlteracao(List<String> campos, String nome, Object anterior, Object novo) {
        if (!java.util.Objects.equals(anterior, novo)) {
            campos.add(nome);
        }
    }

    @Transactional
    public void excluir(UUID id, String responsavelMatricula) {
        validarResponsavel(responsavelMatricula);
        Parceiro parceiro = buscarPorId(id);
        parceiro.setStatus("INATIVO");
        parceiroRepository.save(parceiro);
        registrarHistorico(parceiro, "ALTERACAO_CADASTRAL",
                "Status do parceiro alterado para INATIVO.", responsavelMatricula);
    }
}