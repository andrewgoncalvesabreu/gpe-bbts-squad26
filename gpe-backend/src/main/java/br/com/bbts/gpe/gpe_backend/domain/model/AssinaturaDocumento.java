package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_assinatura_documento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssinaturaDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    @JsonIgnore
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_versao_id", nullable = false)
    @JsonIgnore
    private DocumentoVersao documentoVersao;

    @Column(nullable = false, length = 100)
    private String provedor;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 200)
    private String protocolo;

    @Column(columnDefinition = "TEXT")
    private String detalhe;

    @Column(name = "responsavel_matricula", nullable = false, length = 50)
    private String responsavelMatricula;

    @CreationTimestamp
    @Column(name = "data_evento", nullable = false, updatable = false)
    private LocalDateTime dataEvento;
}
