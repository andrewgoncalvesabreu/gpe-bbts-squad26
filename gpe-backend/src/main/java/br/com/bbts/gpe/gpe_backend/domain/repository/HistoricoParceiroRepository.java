package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoParceiroRepository
        extends JpaRepository<HistoricoParceiro, UUID> {

    List<HistoricoParceiro> findByParceiroIdOrderByDataEventoDesc(
            UUID parceiroId
    );
}