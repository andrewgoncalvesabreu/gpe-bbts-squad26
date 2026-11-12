package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_plano_trabalho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoTrabalho {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false, unique = true)
    @JsonIgnore
    private Projeto projeto;

    @Column(name = "cronograma_inicio", nullable = false)
    private LocalDate cronogramaInicio;

    @Column(name = "cronograma_fim", nullable = false)
    private LocalDate cronogramaFim;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String responsaveis;

    @OneToMany(mappedBy = "planoTrabalho", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanoObjetivo> objetivos = new ArrayList<>();

    @OneToMany(mappedBy = "planoTrabalho", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanoEntrega> entregas = new ArrayList<>();

    @OneToMany(mappedBy = "planoTrabalho", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanoMarco> marcos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
