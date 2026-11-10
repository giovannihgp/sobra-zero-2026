BEGIN;

INSERT INTO usuarios (nome, email, senha_hash, tipo_usuario, documento, endereco, capacidade_kg) VALUES
    ('Administrador do Sistema',
     'admin@sobrazero.com',
     '$2a$10$4mKAofICE3W/M9Taj8lBoORc4x3668RbZPtX9IwsnF2pUZxZVzJHq',
     'ADMINISTRADOR',
     'ADM-0001',
     NULL,
     NULL),
    ('Mercado Bom Preco',
     'mercado@teste.com',
     '$2a$10$I9nrX7dR3RjEsetnqQan5uPmw0a2EEhcPG7UbbXtGgFVJ43DDBEfq',
     'ESTABELECIMENTO',
     '11.111.111/0001-11',
     'Rua das Flores, 120 - Centro',
     NULL),
    ('Instituicao Mesa Solidaria',
     'instituicao@teste.com',
     '$2a$10$YflJsWJMHowFYGqx6i7theo.bas529lKTHAxhIL6iL9YpZyZk4Qam',
     'INSTITUICAO',
     '22.222.222/0001-22',
     NULL,
     500.00);

WITH d AS (
    INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status)
    VALUES ((SELECT id FROM usuarios WHERE email = 'mercado@teste.com'),
            'PERECIVEL', CURRENT_DATE, CURRENT_DATE + 1, TRUE, 'DISPONIVEL')
    RETURNING id
)
INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida)
SELECT d.id, i.descricao, i.quantidade, i.unidade_medida
FROM d
CROSS JOIN (VALUES
    ('Iogurte natural 170g',      24,  'un'),
    ('Queijo mussarela fatiado',  3.5, 'kg')
) AS i (descricao, quantidade, unidade_medida);

WITH d AS (
    INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status)
    VALUES ((SELECT id FROM usuarios WHERE email = 'mercado@teste.com'),
            'PERECIVEL', CURRENT_DATE, CURRENT_DATE + 5, FALSE, 'DISPONIVEL')
    RETURNING id
)
INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida)
SELECT d.id, i.descricao, i.quantidade, i.unidade_medida
FROM d
CROSS JOIN (VALUES
    ('Banana prata',  15, 'kg'),
    ('Tomate',         8, 'kg'),
    ('Alface crespa', 10, 'un')
) AS i (descricao, quantidade, unidade_medida);

WITH d AS (
    INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status, lote, meses_estocagem)
    VALUES ((SELECT id FROM usuarios WHERE email = 'mercado@teste.com'),
            'NAO_PERECIVEL', CURRENT_DATE, NULL, FALSE, 'DISPONIVEL', 'L2026-001', 12)
    RETURNING id
)
INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida)
SELECT d.id, i.descricao, i.quantidade, i.unidade_medida
FROM d
CROSS JOIN (VALUES
    ('Arroz tipo 1',     20, 'kg'),
    ('Feijao carioca',   15, 'kg'),
    ('Macarrao espaguete', 4, 'cx')
) AS i (descricao, quantidade, unidade_medida);

WITH d AS (
    INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status)
    VALUES ((SELECT id FROM usuarios WHERE email = 'mercado@teste.com'),
            'PERECIVEL', CURRENT_DATE - 6, CURRENT_DATE - 2, FALSE, 'DISPONIVEL')
    RETURNING id
)
INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida)
SELECT d.id, i.descricao, i.quantidade, i.unidade_medida
FROM d
CROSS JOIN (VALUES
    ('Pao de forma integral', 30, 'un'),
    ('Pao frances',            6, 'kg')
) AS i (descricao, quantidade, unidade_medida);

WITH d AS (
    INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status)
    VALUES ((SELECT id FROM usuarios WHERE email = 'mercado@teste.com'),
            'PERECIVEL', CURRENT_DATE - 1, CURRENT_DATE + 2, TRUE, 'AGENDADA')
    RETURNING id
),
i AS (
    INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida)
    SELECT d.id, v.descricao, v.quantidade, v.unidade_medida
    FROM d
    CROSS JOIN (VALUES
        ('Frango resfriado', 18, 'kg'),
        ('Presunto fatiado',  4, 'kg')
    ) AS v (descricao, quantidade, unidade_medida)
)
INSERT INTO coletas (doacao_id, instituicao_id, data_agendada, data_retirada, status_coleta)
SELECT d.id,
       (SELECT id FROM usuarios WHERE email = 'instituicao@teste.com'),
       CURRENT_DATE + 1,
       NULL,
       'AGENDADA'
FROM d;

WITH d AS (
    INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status, lote, meses_estocagem)
    VALUES ((SELECT id FROM usuarios WHERE email = 'mercado@teste.com'),
            'NAO_PERECIVEL', CURRENT_DATE - 5, NULL, FALSE, 'CONCLUIDA', 'L2026-002', 18)
    RETURNING id
),
i AS (
    INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida)
    SELECT d.id, v.descricao, v.quantidade, v.unidade_medida
    FROM d
    CROSS JOIN (VALUES
        ('Farinha de trigo', 30, 'kg'),
        ('Acucar cristal',   25, 'kg'),
        ('Oleo de soja',      2, 'cx')
    ) AS v (descricao, quantidade, unidade_medida)
)
INSERT INTO coletas (doacao_id, instituicao_id, data_agendada, data_retirada, status_coleta)
SELECT d.id,
       (SELECT id FROM usuarios WHERE email = 'instituicao@teste.com'),
       CURRENT_DATE - 3,
       (CURRENT_DATE - 3) + TIME '14:30',
       'CONCLUIDA'
FROM d;

COMMIT;
