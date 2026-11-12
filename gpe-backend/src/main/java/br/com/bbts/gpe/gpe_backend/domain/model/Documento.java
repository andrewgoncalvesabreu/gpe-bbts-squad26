package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_documento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id")
    @JsonIgnore
    private Parceiro parceiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oportunidade_id")
    @JsonIgnore
    private Oportunidade oportunidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id")
    @JsonIgnore
    private InstrumentoJuridico instrumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceria_id")
    @JsonIgnore
    private Parceria parceria;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "versao_atual", nullable = false)
    private Integer versaoAtual;

    @Column(name = "status_assinatura", nullable = false, length = 40)
    private String statusAssinatura;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (dataAtualizacao == null) dataAtualizacao = dataCriacao;
        if (versaoAtual == null) versaoAtual = 1;
        if (statusAssinatura == null) statusAssinatura = "NAO_ENVIADO";
        ativo = true;
    }

    @PreUpdate
    void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
