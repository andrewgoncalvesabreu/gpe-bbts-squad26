package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.HistoricoParceiroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HistoricoParceiroService {

    private final HistoricoParceiroRepository historicoRepository;

    public HistoricoParceiroService(
            HistoricoParceiroRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public List<HistoricoParceiro> listarPorParceiro(UUID parceiroId) {

        return historicoRepository
                .findByParceiroIdOrderByDataEventoDesc(parceiroId);
    }

    public HistoricoParceiro salvar(HistoricoParceiro historico) {

        if (historico.getDataEvento() == null) {
            historico.setDataEvento(LocalDateTime.now());
        }

        return historicoRepository.save(historico);
    }
}