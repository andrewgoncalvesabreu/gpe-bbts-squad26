package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParceriaRepository extends JpaRepository<Parceria, UUID> {
    boolean existsByInstrumentoId(UUID instrumentoId);

    List<Parceria> findAllByOrderByDataCriacaoDesc();

    List<Parceria> findByParceiroIdOrderByDataCriacaoDesc(UUID parceiroId);
}
