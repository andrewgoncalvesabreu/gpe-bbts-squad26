package br.com.bbts.gpe.gpe_backend.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_parceiro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "tipo_organizacao", nullable = false, length = 50)
    private String tipoOrganizacao;

    @Column(name = "segmento_atuacao", length = 100)
    private String segmentoAtuacao;

    @Column(length = 50)
    private String porte;

    @Column(name = "email_contato", length = 100)
    private String emailContato;

    @Column(length = 20)
    private String telefone;

    @Column(name = "endereco_completo", columnDefinition = "TEXT")
    private String enderecoCompleto;

    @Column(nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
}