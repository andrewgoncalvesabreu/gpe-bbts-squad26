package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_parceria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parceria {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id", nullable = false)
    @JsonIgnore
    private Parceiro parceiro;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false, unique = true)
    @JsonIgnore
    private InstrumentoJuridico instrumento;

    @Column(nullable = false, length = 30)
    private String status;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
