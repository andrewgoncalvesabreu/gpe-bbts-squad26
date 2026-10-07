package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OportunidadeRepository extends JpaRepository<Oportunidade, UUID> {

    // Garanta que o nome do atributo na classe Oportunidade é "parceiro" e em Parceiro é "id"
    List<Oportunidade> findByParceiroIdOrderByDataCriacaoDesc(UUID parceiroId);

}