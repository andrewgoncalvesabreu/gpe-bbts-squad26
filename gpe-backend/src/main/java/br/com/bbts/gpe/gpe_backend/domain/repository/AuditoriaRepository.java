package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {

    @Query("""
        select a from Auditoria a
        where (:usuario is null or lower(a.usuario) like lower(concat('%', :usuario, '%')))
          and (:operacao is null or a.operacao = :operacao)
          and (:entidade is null or a.entidade = :entidade)
          and (:inicio is null or a.dataHora >= :inicio)
          and (:fim is null or a.dataHora <= :fim)
        order by a.dataHora desc
        """)
    List<Auditoria> consultar(
            @Param("usuario") String usuario,
            @Param("operacao") String operacao,
            @Param("entidade") String entidade,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
