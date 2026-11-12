package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.PlanoTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanoTrabalhoRepository extends JpaRepository<PlanoTrabalho, UUID> {
    Optional<PlanoTrabalho> findByProjetoId(UUID projetoId);
}
