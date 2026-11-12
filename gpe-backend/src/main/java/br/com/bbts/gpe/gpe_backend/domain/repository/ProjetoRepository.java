package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjetoRepository extends JpaRepository<Projeto, UUID> {
    List<Projeto> findByParceriaIdOrderByDataCriacaoDesc(UUID parceriaId);

    List<Projeto> findAllByOrderByDataCriacaoDesc();
}
