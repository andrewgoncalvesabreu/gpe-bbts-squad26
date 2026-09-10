-- Tabela de Parceiros
CREATE TABLE tb_parceiro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razao_social VARCHAR(200) NOT NULL,
    nome_fantasia VARCHAR(200),
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    tipo_organizacao VARCHAR(50) NOT NULL,
    segmento_atuacao VARCHAR(100),
    porte VARCHAR(50),
    email_contato VARCHAR(100),
    telefone VARCHAR(20),
    endereco_completo TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'EM_ANALISE',
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Oportunidades
CREATE TABLE tb_oportunidade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(200) NOT NULL,
    parceiro_id UUID NOT NULL,
    origem VARCHAR(50) NOT NULL,
    problema_demandado TEXT,
    solucao_proposta TEXT,
    estimativa_financeira DECIMAL(15, 2),
    status_workflow VARCHAR(50) NOT NULL DEFAULT 'TRIAGEM',
    area_demandante VARCHAR(100),
    responsavel_matricula VARCHAR(50),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_oportunidade_parceiro FOREIGN KEY (parceiro_id) REFERENCES tb_parceiro(id)
);

-- Índices de Performance
CREATE INDEX idx_parceiro_cnpj ON tb_parceiro(cnpj);
CREATE INDEX idx_oportunidade_parceiro ON tb_oportunidade(parceiro_id);
CREATE INDEX idx_oportunidade_status ON tb_oportunidade(status_workflow);