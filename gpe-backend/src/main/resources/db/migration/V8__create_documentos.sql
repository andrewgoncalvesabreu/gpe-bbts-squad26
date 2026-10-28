CREATE TABLE tb_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    parceiro_id UUID,
    oportunidade_id UUID,
    instrumento_id UUID,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_documento_parceiro FOREIGN KEY (parceiro_id) REFERENCES tb_parceiro(id),
    CONSTRAINT fk_documento_oportunidade FOREIGN KEY (oportunidade_id) REFERENCES tb_oportunidade(id),
    CONSTRAINT fk_documento_instrumento FOREIGN KEY (instrumento_id) REFERENCES tb_instrumento_juridico(id),
    CONSTRAINT ck_documento_vinculo CHECK (
        parceiro_id IS NOT NULL OR oportunidade_id IS NOT NULL OR instrumento_id IS NOT NULL
    )
);

CREATE INDEX idx_documento_parceiro ON tb_documento(parceiro_id);
CREATE INDEX idx_documento_oportunidade ON tb_documento(oportunidade_id);
CREATE INDEX idx_documento_instrumento ON tb_documento(instrumento_id);

CREATE TABLE tb_documento_versao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    numero_versao INTEGER NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_versao_documento FOREIGN KEY (documento_id) REFERENCES tb_documento(id) ON DELETE CASCADE,
    CONSTRAINT uk_documento_numero_versao UNIQUE (documento_id, numero_versao)
);

CREATE INDEX idx_versao_documento ON tb_documento_versao(documento_id);