CREATE TABLE tb_auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario VARCHAR(150) NOT NULL,
    operacao VARCHAR(50) NOT NULL,
    entidade VARCHAR(100) NOT NULL,
    entidade_id UUID,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conteudo_anterior TEXT,
    conteudo_novo TEXT,
    ip_origem VARCHAR(45)
);

CREATE INDEX idx_auditoria_data_hora ON tb_auditoria(data_hora DESC);
CREATE INDEX idx_auditoria_usuario ON tb_auditoria(usuario);
CREATE INDEX idx_auditoria_operacao ON tb_auditoria(operacao);
CREATE INDEX idx_auditoria_entidade ON tb_auditoria(entidade, entidade_id);
