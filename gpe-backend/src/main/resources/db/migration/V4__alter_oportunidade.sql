ALTER TABLE tb_oportunidade
    ADD COLUMN IF NOT EXISTS descricao TEXT,
    ADD COLUMN IF NOT EXISTS objetivos TEXT,
    ADD COLUMN IF NOT EXISTS justificativa TEXT,
    ADD COLUMN IF NOT EXISTS beneficios_esperados TEXT;