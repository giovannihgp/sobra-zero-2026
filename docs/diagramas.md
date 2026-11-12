# Diagramas do Sobra Zero

Versão atualizada dos diagramas, alinhada ao código em `src/main/java/com/sobrazero/model/` e ao script `database/schema.sql`. As imagens `diagrama_classes.png` e `diagrama_der.png` desta pasta são as versões entregues no documento da Entrega 1. O que mudou desde então está na seção "Ajustes em relação ao documento da Entrega 1" do [README](../README.md).

## Diagrama de banco de dados (DER)

```mermaid
erDiagram
    usuarios ||--o{ doacoes : "estabelecimento publica"
    usuarios ||--o{ coletas : "instituicao agenda"
    doacoes ||--o{ itens_doacao : contem
    doacoes ||--o| coletas : gera

    usuarios {
        serial id PK
        varchar nome "VARCHAR(100) NOT NULL"
        varchar email UK "VARCHAR(120) NOT NULL"
        varchar senha_hash "VARCHAR(255) NOT NULL"
        varchar tipo_usuario "ESTABELECIMENTO, INSTITUICAO ou ADMINISTRADOR"
        varchar documento "VARCHAR(18): CNPJ ou matricula"
        varchar endereco "VARCHAR(150) NULL"
        numeric capacidade_kg "NUMERIC(10,2) NULL"
    }

    doacoes {
        serial id PK
        integer estabelecimento_id FK "NOT NULL"
        varchar tipo_doacao "PERECIVEL ou NAO_PERECIVEL"
        date data_cadastro "NOT NULL, padrao CURRENT_DATE"
        date data_validade "NULL"
        boolean exige_refrigeracao "NOT NULL, padrao FALSE"
        varchar status "DISPONIVEL, AGENDADA, CONCLUIDA ou EXPIRADA"
        varchar lote "VARCHAR(30) NULL"
        integer meses_estocagem "NULL"
    }

    itens_doacao {
        serial id PK
        integer doacao_id FK "NOT NULL, ON DELETE CASCADE"
        varchar descricao "VARCHAR(120) NOT NULL"
        numeric quantidade "NUMERIC(10,2) NOT NULL"
        varchar unidade_medida "VARCHAR(10) NOT NULL"
    }

    coletas {
        serial id PK
        integer doacao_id FK, UK "NOT NULL"
        integer instituicao_id FK "NOT NULL"
        date data_agendada "NOT NULL"
        timestamp data_retirada "NULL"
        varchar status_coleta "AGENDADA, CONCLUIDA ou CANCELADA"
    }
```

- **`usuarios`** guarda os três perfis numa tabela só, diferenciados por `tipo_usuario`.
- **`itens_doacao.doacao_id`** tem `ON DELETE CASCADE`: excluir uma doação apaga os itens dela (composição forte).
- **`coletas.doacao_id`** é `UNIQUE`: uma doação nunca é reservada duas vezes (RF04).

## Diagrama de classes (UML)

```mermaid
classDiagram
    direction TB

    class Usuario {
        <<abstract>>
        -int id
        -String nome
        -String email
        -String senhaHash
        +autenticar(String senha) boolean
        +obterPerfil() String*
    }

    class Estabelecimento {
        -String cnpj
        -String endereco
        -List~Doacao~ doacoes
        +registrarDoacao(Doacao doacao) void
        +obterPerfil() String
    }

    class Instituicao {
        -String cnpj
        -double capacidadeKg
        +agendarColeta(Doacao doacao, LocalDate dataAgendada) Coleta
        +obterPerfil() String
    }

    class Administrador {
        -String matricula
        +gerarRelatorio(LocalDate inicio, LocalDate fim) Relatorio
        +obterPerfil() String
    }

    class Doacao {
        <<abstract>>
        -int id
        -LocalDate dataCadastro
        -StatusDoacao status
        -Estabelecimento estabelecimento
        -List~ItemDoacao~ itens
        +adicionarItem(ItemDoacao item) void
        +calcularPesoTotal() double
        +calcularPrioridade() int*
        +descreverManuseio() String*
    }

    class DoacaoPerecivel {
        -LocalDate dataValidade
        -boolean exigeRefrigeracao
        +calcularPrioridade() int
        +descreverManuseio() String
    }

    class DoacaoNaoPerecivel {
        -String lote
        -int mesesEstocagem
        +calcularPrioridade() int
        +descreverManuseio() String
    }

    class ItemDoacao {
        -int id
        -String descricao
        -double quantidade
        -String unidade
    }

    class Coleta {
        -int id
        -Doacao doacao
        -Instituicao instituicao
        -LocalDate dataAgendada
        -LocalDateTime dataRetirada
        -String statusColeta
        +confirmarRetirada() void
    }

    class Relatorio {
        -LocalDate dataInicio
        -LocalDate dataFim
        -double pesoTotalKg
        -Map~StatusDoacao, Integer~ quantidadePorStatus
        -Map~String, Double~ pesoPorEstabelecimento
        -Map~String, Double~ pesoPorInstituicao
    }

    class StatusDoacao {
        <<enumeration>>
        DISPONIVEL
        AGENDADA
        CONCLUIDA
        EXPIRADA
    }

    Usuario <|-- Estabelecimento
    Usuario <|-- Instituicao
    Usuario <|-- Administrador
    Doacao <|-- DoacaoPerecivel
    Doacao <|-- DoacaoNaoPerecivel

    Estabelecimento "1" *-- "*" Doacao : registra
    Doacao "1" *-- "*" ItemDoacao : composta por
    Instituicao "1" --> "*" Coleta : agenda
    Coleta "*" --> "1" Doacao : refere-se a
    Doacao --> StatusDoacao
    Administrador ..> Relatorio : gera
```

- **Herança:** `Usuario` e `Doacao` são abstratas. Os métodos em itálico são abstratos e sobrescritos com `@Override` nas subclasses.
- **Polimorfismo:** `calcularPrioridade()` e `descreverManuseio()` têm uma lógica diferente em cada subclasse de `Doacao`. O `PriorizacaoService` ordena uma `List<Doacao>` sem saber o tipo de cada uma.
- **Composição:** os losangos preenchidos indicam que `Doacao` só existe dentro de um `Estabelecimento` e `ItemDoacao` só existe dentro de uma `Doacao`.
