package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_plano_objetivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoObjetivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_trabalho_id", nullable = false)
    @JsonIgnore
    private PlanoTrabalho planoTrabalho;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;
}
