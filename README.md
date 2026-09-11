# Sobra Zero

Sistema de intermediação de doação de excedentes alimentares entre estabelecimentos do varejo de bairro e instituições sociais, com priorização automática de coleta por proximidade da data de validade.

Projeto desenvolvido para a **AEP (Atividade de Estudo Prático)** do 4º semestre de Engenharia de Software — Unicesumar, Maringá/PR — 2026.2.

**ODS contemplada:** ODS 2 — Fome Zero e Agricultura Sustentável (com aderência à meta 12.3 da ODS 12, redução do desperdício de alimentos no varejo).

---

## O problema

Mercados de bairro descartam diariamente alimentos que ainda estão próprios para consumo: hortifrúti fora do padrão estético, pães do dia e produtos a um ou dois dias do vencimento. A poucos quarteirões, instituições sociais enfrentam dificuldade para montar o cardápio da semana. Hoje a ponte entre os dois lados é informal — grupo de WhatsApp, telefonema — sem registro, sem controle de prazo e sem garantia de que alguém vai buscar a tempo.

O Sobra Zero organiza esse fluxo: o estabelecimento publica o excedente, o sistema ordena as doações pela urgência, a instituição reserva a coleta e o histórico fica registrado para medir o impacto.

---

## Equipe

| Integrante | RA | Usuário GitHub | Responsabilidade principal |
|---|---|---|---|
| Giovanni Henrique Gazin Pasa | 25355659-2 | @giovannihgp | Controle de acesso e relatórios |
| Gustavo Henrique Portel de Almeida | 25364235-2 | @BoyThaCookies | CRUD de doações e itens |
| Gabriel Aguitoni Lázaro De Azevedo | 24503163-2 | @GnosisDodecaedru | Priorização e agendamento de coletas |

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (LTS) |
| Build / dependências | Maven |
| Banco de dados | PostgreSQL 16 |
| Acesso a dados | JDBC com padrão DAO |
| Interface | Console (terminal) |

---

## Estrutura do repositório

```
sobra-zero/
├── src/          # código-fonte Java (model, dao, service, view)
├── docs/         # documento da AEP, diagramas (UML e DER) e atas de sprint
├── database/     # schema.sql (criação das tabelas) e seed.sql (dados de teste)
└── README.md
```

---

## Como executar

### Pré-requisitos

- JDK 21 ou superior (`java -version` deve retornar 21+)
- Maven 3.9+ (`mvn -version`)
- PostgreSQL 16 rodando localmente na porta padrão `5432`

### 1. Clonar o repositório

```bash
git clone https://github.com/giovannihgp/sobra-zero-2026.git
cd sobra-zero-2026
```

### 2. Criar o banco de dados

Com o PostgreSQL em execução, crie o banco e rode o script de criação das tabelas:

```bash
createdb -U postgres sobrazero
psql -U postgres -d sobrazero -f database/schema.sql
```

Para carregar dados de exemplo (usuários e doações de teste), rode também:

```bash
psql -U postgres -d sobrazero -f database/seed.sql
```

> No Windows, se os comandos `createdb` e `psql` não forem reconhecidos, use o **pgAdmin**: crie um banco chamado `sobrazero`, abra a Query Tool e execute o conteúdo dos arquivos `.sql` da pasta `database/`.

### 3. Configurar a conexão

Abra o arquivo `src/main/resources/config.properties` e ajuste o usuário e a senha do seu PostgreSQL:

```properties
db.url=jdbc:postgresql://localhost:5432/sobrazero
db.user=postgres
db.password=SUA_SENHA_AQUI
```

### 4. Compilar e rodar

```bash
mvn clean package
java -jar target/sobra-zero-1.0.jar
```

A aplicação abre um menu no terminal. Use as credenciais de teste abaixo para navegar pelos três perfis.

---

## Credenciais de teste

Disponíveis após a execução do `seed.sql`:

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@sobrazero.com | admin123 |
| Estabelecimento | mercado@teste.com | teste123 |
| Instituição | instituicao@teste.com | teste123 |

---

## Funcionalidades (escopo do 2º bimestre)

- [ ] **RF01** — Cadastro e autenticação de usuários nos perfis Estabelecimento, Instituição e Administrador
- [ ] **RF02** — Registro de doações (perecíveis e não perecíveis) com seus respectivos itens
- [ ] **RF03** — Cálculo automático da prioridade de coleta por proximidade da validade
- [ ] **RF04** — Reserva de doação pela instituição, com bloqueio para as demais
- [ ] **RF05** — Confirmação de retirada com registro de data e hora
- [ ] **RF06** — Expiração automática de doações não retiradas até a validade
- [ ] **RF07** — Relatórios consolidados por período com exportação em CSV

---

## Organização do trabalho

Cada integrante desenvolve em uma branch própria, nomeada por funcionalidade (`feature/controle-acesso`, `feature/doacoes`, `feature/coletas`), e integra na `main` através de Pull Request revisado por outro membro da equipe.

| Sprint | Período | Entrega |
|---|---|---|
| 1 | 14/09 a 27/09 | Fundação e controle de acesso |
| 2 | 28/09 a 11/10 | Núcleo de doações (CRUD) |
| 3 | 12/10 a 25/10 | Priorização e agendamento |
| 4 | 26/10 a 08/11 | Relatórios, expiração e fechamento |

---

## Documentação

O documento completo de arquitetura e requisitos, com os diagramas de classes (UML) e do banco de dados (DER), está em [`docs/`](docs/).