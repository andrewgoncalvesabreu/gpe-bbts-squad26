package br.com.bbts.gpe.gpe_backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String usuario;

    @Column(nullable = false, length = 50)
    private String operacao;

    @Column(nullable = false, length = 100)
    private String entidade;

    @Column(name = "entidade_id")
    private UUID entidadeId;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "conteudo_anterior", columnDefinition = "TEXT")
    private String conteudoAnterior;

    @Column(name = "conteudo_novo", columnDefinition = "TEXT")
    private String conteudoNovo;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @PrePersist
    void definirDataHora() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
