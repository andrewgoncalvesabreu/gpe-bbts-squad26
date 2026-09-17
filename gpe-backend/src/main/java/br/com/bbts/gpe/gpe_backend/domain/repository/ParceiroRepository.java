package br.com.bbts.gpe.gpe_backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;

import java.util.UUID;

@Repository
public interface ParceiroRepository extends JpaRepository<Parceiro, UUID> {
    boolean existsByCnpj(String cnpj);
}