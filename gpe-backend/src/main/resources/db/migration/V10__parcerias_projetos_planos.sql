CREATE TABLE tb_parceria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    parceiro_id UUID NOT NULL,
    instrumento_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'ATIVA',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parceria_parceiro
        FOREIGN KEY (parceiro_id) REFERENCES tb_parceiro(id),
    CONSTRAINT fk_parceria_instrumento
        FOREIGN KEY (instrumento_id) REFERENCES tb_instrumento_juridico(id)
);

CREATE INDEX idx_parceria_parceiro ON tb_parceria(parceiro_id);

CREATE TABLE tb_projeto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parceria_id UUID NOT NULL,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ATIVO',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_projeto_parceria
        FOREIGN KEY (parceria_id) REFERENCES tb_parceria(id)
);

CREATE INDEX idx_projeto_parceria ON tb_projeto(parceria_id);

CREATE TABLE tb_plano_trabalho (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    projeto_id UUID NOT NULL UNIQUE,
    cronograma_inicio DATE NOT NULL,
    cronograma_fim DATE NOT NULL,
    responsaveis TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plano_projeto
        FOREIGN KEY (projeto_id) REFERENCES tb_projeto(id) ON DELETE CASCADE,
    CONSTRAINT ck_plano_cronograma
        CHECK (cronograma_fim >= cronograma_inicio)
);

CREATE TABLE tb_plano_objetivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_trabalho_id UUID NOT NULL,
    descricao TEXT NOT NULL,
    CONSTRAINT fk_objetivo_plano
        FOREIGN KEY (plano_trabalho_id) REFERENCES tb_plano_trabalho(id) ON DELETE CASCADE
);

CREATE TABLE tb_plano_entrega (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_trabalho_id UUID NOT NULL,
    descricao TEXT NOT NULL,
    data_prevista DATE NOT NULL,
    responsavel VARCHAR(100) NOT NULL,
    CONSTRAINT fk_entrega_plano
        FOREIGN KEY (plano_trabalho_id) REFERENCES tb_plano_trabalho(id) ON DELETE CASCADE
);

CREATE TABLE tb_plano_marco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_trabalho_id UUID NOT NULL,
    descricao TEXT NOT NULL,
    data_prevista DATE NOT NULL,
    CONSTRAINT fk_marco_plano
        FOREIGN KEY (plano_trabalho_id) REFERENCES tb_plano_trabalho(id) ON DELETE CASCADE
);
