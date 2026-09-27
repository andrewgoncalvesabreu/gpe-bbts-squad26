package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParceiroRepository extends JpaRepository<Parceiro, UUID> {

    boolean existsByCnpj(String cnpj);

    Optional<Parceiro> findByCnpj(String cnpj);
}