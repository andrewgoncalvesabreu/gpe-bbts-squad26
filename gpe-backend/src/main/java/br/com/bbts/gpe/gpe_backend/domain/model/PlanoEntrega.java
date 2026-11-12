package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_plano_entrega")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_trabalho_id", nullable = false)
    @JsonIgnore
    private PlanoTrabalho planoTrabalho;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(nullable = false, length = 100)
    private String responsavel;
}
