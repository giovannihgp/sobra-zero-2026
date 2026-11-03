-- Sobra Zero - schema do banco de dados
-- Reflete o DER (Figura 2) do documento em docs/AEP1_SobraZero.pdf

CREATE TABLE usuarios (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    senha_hash    VARCHAR(255) NOT NULL,
    tipo_usuario  VARCHAR(20) NOT NULL
        CHECK (tipo_usuario IN ('ESTABELECIMENTO', 'INSTITUICAO', 'ADMINISTRADOR')),
    documento     VARCHAR(18)
);

CREATE TABLE doacoes (
    id                  SERIAL PRIMARY KEY,
    estabelecimento_id  INTEGER NOT NULL REFERENCES usuarios(id),
    tipo_doacao         VARCHAR(20) NOT NULL
        CHECK (tipo_doacao IN ('PERECIVEL', 'NAO_PERECIVEL')),
    data_cadastro       DATE NOT NULL DEFAULT CURRENT_DATE,
    data_validade       DATE NULL,
    exige_refrigeracao  BOOLEAN NOT NULL DEFAULT FALSE,
    status              VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL'
        CHECK (status IN ('DISPONIVEL', 'AGENDADA', 'CONCLUIDA', 'EXPIRADA'))
);

-- ON DELETE CASCADE traduz no banco a composicao forte Doacao -> ItemDoacao do diagrama de classes
CREATE TABLE itens_doacao (
    id              SERIAL PRIMARY KEY,
    doacao_id       INTEGER NOT NULL REFERENCES doacoes(id) ON DELETE CASCADE,
    descricao       VARCHAR(120) NOT NULL,
    quantidade      NUMERIC(10,2) NOT NULL,
    unidade_medida  VARCHAR(10) NOT NULL
);

-- UNIQUE em doacao_id garante no banco que uma doacao nunca seja reservada duas vezes (RF04)
CREATE TABLE coletas (
    id               SERIAL PRIMARY KEY,
    doacao_id        INTEGER NOT NULL UNIQUE REFERENCES doacoes(id),
    instituicao_id   INTEGER NOT NULL REFERENCES usuarios(id),
    data_agendada    DATE NOT NULL,
    data_retirada    TIMESTAMP NULL,
    status_coleta    VARCHAR(20) NOT NULL DEFAULT 'AGENDADA'
        CHECK (status_coleta IN ('AGENDADA', 'CONCLUIDA', 'CANCELADA'))
);
