CREATE TABLE tb_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parceiro_id UUID NOT NULL,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    versao_atual INTEGER NOT NULL DEFAULT 1 CHECK (versao_atual > 0),
    status_assinatura VARCHAR(40) NOT NULL DEFAULT 'NAO_ENVIADO',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documento_parceiro
        FOREIGN KEY (parceiro_id) REFERENCES tb_parceiro(id)
);

CREATE INDEX idx_documento_parceiro ON tb_documento(parceiro_id);

CREATE TABLE tb_documento_versao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    numero_versao INTEGER NOT NULL CHECK (numero_versao > 0),
    nome_arquivo VARCHAR(255) NOT NULL,
    chave_armazenamento VARCHAR(100) NOT NULL UNIQUE,
    tipo_mime VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT NOT NULL CHECK (tamanho_bytes > 0 AND tamanho_bytes <= 20971520),
    sha256 VARCHAR(64) NOT NULL,
    responsavel_matricula VARCHAR(50) NOT NULL,
    data_upload TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documento_versao_documento
        FOREIGN KEY (documento_id) REFERENCES tb_documento(id) ON DELETE CASCADE,
    CONSTRAINT uk_documento_numero_versao UNIQUE (documento_id, numero_versao)
);

CREATE TABLE tb_assinatura_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    documento_versao_id UUID NOT NULL,
    provedor VARCHAR(100) NOT NULL,
    status VARCHAR(40) NOT NULL,
    protocolo VARCHAR(200),
    detalhe TEXT,
    responsavel_matricula VARCHAR(50) NOT NULL,
    data_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assinatura_documento
        FOREIGN KEY (documento_id) REFERENCES tb_documento(id) ON DELETE CASCADE,
    CONSTRAINT fk_assinatura_versao
        FOREIGN KEY (documento_versao_id) REFERENCES tb_documento_versao(id)
);

CREATE INDEX idx_assinatura_documento_data
    ON tb_assinatura_documento(documento_id, data_evento DESC);
