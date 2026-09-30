package br.com.bbts.gpe.gpe_backend.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Razão Social é obrigatória")
    @Size(max = 200, message = "Razão Social deve ter no máximo 200 caracteres")
    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Size(max = 200, message = "Nome Fantasia deve ter no máximo 200 caracteres")
    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    @NotBlank(message = "CNPJ é obrigatório")
    @Size(min = 14, max = 14, message = "CNPJ deve possuir 14 caracteres")
    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @NotNull(message = "Tipo de Organização é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_organizacao", nullable = false, length = 50)
    private TipoOrganizacao tipoOrganizacao;

    @Size(max = 100, message = "Segmento de atuação deve ter no máximo 100 caracteres")
    @Column(name = "segmento_atuacao", length = 100)
    private String segmentoAtuacao;

    @Size(max = 50, message = "Porte deve ter no máximo 50 caracteres")
    @Column(length = 50)
    private String porte;

    @Email(message = "E-mail inválido")
    @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
    @Column(name = "email_contato", length = 100)
    private String emailContato;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String telefone;

    @Column(name = "endereco_completo", columnDefinition = "TEXT")
    private String enderecoCompleto;

    @Size(max = 500, message = "Responsáveis deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String responsaveis;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = 50, message = "Status deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
}