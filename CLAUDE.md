# Contexto do projeto — Sobra Zero

Sistema de intermediação de doação de excedentes alimentares entre estabelecimentos do varejo e instituições sociais, com priorização de coleta por proximidade da data de validade.

Trabalho acadêmico (AEP) do 4º semestre de Engenharia de Software — Unicesumar. O documento completo de requisitos e arquitetura está em `docs/`. Leia-o antes de propor qualquer mudança estrutural.

**Idioma:** todo o código, comentários, mensagens de commit e nomes de variáveis em português (sem acentos em identificadores).

---

## Stack fixa

Estas escolhas estão justificadas no documento entregue ao professor e **não devem ser trocadas**:

- Java 21 (LTS)
- Maven para build e dependências
- PostgreSQL 16
- JDBC puro com padrão DAO — **sem ORM** (nada de Hibernate, JPA ou Spring Data)
- Interface de console (terminal), sem framework web

Dependências permitidas no `pom.xml`: driver do PostgreSQL e biblioteca de hash de senha (jBCrypt). Não adicione outras sem necessidade real.

---

## Arquitetura em camadas

```
src/main/java/com/sobrazero/
├── model/      # entidades de domínio (POJOs, sem SQL, sem I/O)
├── dao/        # acesso a dados: todo o SQL vive aqui
├── service/    # regras de negócio
├── view/       # menus de console e leitura de entrada
└── util/       # conexão com o banco, helpers
```

Regras de dependência entre camadas:

- `view` chama `service`. Nunca chama `dao` direto.
- `service` chama `dao`. Nunca imprime nada no console.
- `dao` só conversa com o banco. Nunca contém regra de negócio.
- `model` não depende de nenhuma outra camada.

Nenhum `System.out.println` fora da camada `view`. Nenhuma string SQL fora da camada `dao`.

---

## Divisão do trabalho (dono de cada arquivo)

Cada integrante só edita os próprios arquivos. Antes de alterar um arquivo de outra pessoa, pare e me pergunte.

| Integrante | Branch | Arquivos |
|---|---|---|
| Giovanni | `feature/controle-acesso` | `pom.xml`, `util/ConexaoBD.java`, `dao/UsuarioDAO.java`, `service/AutenticacaoService.java`, `service/RelatorioService.java`, `view/MenuPrincipal.java`, `view/MenuLogin.java`, `view/MenuAdministrador.java` |
| Gustavo | `feature/doacoes` | todo o `model/` (Bloco 0), `dao/DoacaoDAO.java`, `dao/ItemDoacaoDAO.java`, `service/DoacaoService.java`, `view/MenuEstabelecimento.java` |
| Gabriel | `feature/coletas` | `database/schema.sql`, `database/seed.sql`, corpo dos métodos de `model/DoacaoPerecivel.java` e `model/DoacaoNaoPerecivel.java`, `dao/ColetaDAO.java`, `service/PriorizacaoService.java`, `service/ColetaService.java`, `view/MenuInstituicao.java` |

- `model/` fica congelado depois do Bloco 0: qualquer mudança é avisada no grupo antes.
- No Bloco 0, `calcularPrioridade()` e `descreverManuseio()` são stubs nas subclasses (`return 0;` / `return "";`). Quem implementa é o Gabriel.
- Menus de outros integrantes que ainda não existem são chamados por classe vazia provisória, para ninguém ficar bloqueado.

---

## Exigências acadêmicas (não negociáveis)

O projeto é avaliado sobre estes pontos. Toda alteração precisa preservá-los:

1. **Classe abstrata / interface:** `Usuario` e `Doacao` são abstratas. Não transforme em classe concreta.
2. **Herança:** `Estabelecimento`, `Instituicao` e `Administrador` estendem `Usuario`. `DoacaoPerecivel` e `DoacaoNaoPerecivel` estendem `Doacao`.
3. **Polimorfismo:** `calcularPrioridade()` e `descreverManuseio()` são declarados em `Doacao` e sobrescritos com `@Override` nas subclasses. A camada `service` deve trabalhar com `List<Doacao>` e nunca usar `instanceof` para decidir comportamento — o polimorfismo é o ponto avaliado.
4. **Composição 1:N:** `Estabelecimento` mantém `List<Doacao>`; `Doacao` mantém `List<ItemDoacao>`.
5. **Encapsulamento:** todos os atributos `private`, com getters e setters. O `status` da doação só muda por métodos de transição na camada `service`.
6. **Persistência real:** todos os dados vão para o PostgreSQL. **Nunca** simule o banco com `ArrayList` em memória, arquivo `.txt` ou serialização — isso zera o critério de persistência da disciplina.
7. **CRUD completo** sobre a entidade principal (`Doacao`): criar, listar, editar e excluir.

---

## Convenções de código

- Classes em `PascalCase`, métodos e variáveis em `camelCase`, constantes em `UPPER_SNAKE_CASE`.
- Tabelas e colunas do banco em `snake_case` minúsculo.
- Status como `enum` (`StatusDoacao`: `DISPONIVEL`, `AGENDADA`, `CONCLUIDA`, `EXPIRADA`), nunca como string solta.
- Todo SQL com `PreparedStatement` e parâmetros — jamais concatenar valor de entrada dentro da query.
- Fechar `Connection`, `PreparedStatement` e `ResultSet` com try-with-resources.
- Senhas sempre em hash. Nunca gravar senha em texto puro, nem imprimir no console.
- O único lugar onde é aceitável decidir por tipo é o `switch` sobre `tipo_usuario` / `tipo_doacao` dentro do DAO, para instanciar a subclasse certa ao ler do banco.
- Inserção de doação e itens acontece na mesma transação: `setAutoCommit(false)`, `commit()` no fim, `rollback()` no `catch`. O id gerado vem de `getGeneratedKeys()`.
- Nenhum stack trace chega ao console: a `view` mostra mensagem amigável. Violação do `UNIQUE` em `coletas` vira "doação já reservada".
- Credenciais do banco ficam em `src/main/resources/config.properties`, que **não** vai para o repositório (adicionar ao `.gitignore`; versionar apenas `config.properties.example`).

---

## Modelo de dados

Quatro tabelas, definidas em `database/schema.sql`:

- `usuarios` — os três perfis em tabela única, discriminados pela coluna `tipo_usuario`.
- `doacoes` — FK `estabelecimento_id`, coluna `tipo_doacao` espelhando a hierarquia, coluna `status` controlando o ciclo de vida.
- `itens_doacao` — FK `doacao_id` com `ON DELETE CASCADE` (traduz a composição forte do modelo).
- `coletas` — FK `doacao_id` com restrição `UNIQUE` (garante no banco que uma doação nunca seja reservada duas vezes) e FK `instituicao_id`.

`database/seed.sql` traz os dados de teste com as credenciais do README (senhas já em hash BCrypt). A validação final é sempre num banco zerado, rodando só `schema.sql` e `seed.sql` — é assim que o professor vai executar.

Qualquer alteração de esquema tem que ser refletida ao mesmo tempo em `database/schema.sql`, nas classes de `model/` e no DER em `docs/`. Os três não podem divergir.

---

## Escopo (requisitos do documento entregue)

- **RF01** — CRUD e autenticação de usuários nos três perfis
- **RF02** — Registro de doação com itens (perecível exige data de validade)
- **RF03** — Cálculo automático de prioridade por proximidade da validade
- **RF04** — Reserva de doação pela instituição, com bloqueio para as demais
- **RF05** — Confirmação de retirada com data e hora
- **RF06** — Expiração automática de doações vencidas
- **RF07** — Relatórios por período com exportação em CSV

Não implemente funcionalidades fora desta lista sem que eu peça. O critério de "aderência ao planejamento" compara o código entregue com este escopo.

---

## Git

- Commits no padrão Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`, `test:`.
- Mensagem em português, verbo no imperativo, sem acentos: `feat: implementa cadastro de doacao perecivel`.
- Uma branch por funcionalidade: `feature/controle-acesso`, `feature/doacoes`, `feature/coletas`.
- Depois do Bloco 0, nada de commit direto na `main`: tudo entra por Pull Request revisado por outro integrante.
- Sempre `git pull origin main` antes de começar a trabalhar.
- Commits pequenos e frequentes. O histórico é avaliado como prova de participação individual — **nunca** faça commits em nome de outro integrante nem junte o trabalho de todos em um commit só.
- Não adicione coautoria nem qualquer menção a ferramentas de IA nas mensagens de commit.

---

## Como trabalhar comigo

- Antes de escrever código novo, verifique como as camadas existentes já resolvem problemas parecidos e siga o mesmo padrão.
- Prefira alterações pequenas e explicadas. Quando uma decisão tiver alternativa razoável, apresente as duas em vez de escolher sozinho.
- Explique o que o código faz enquanto escreve: eu preciso conseguir defender cada parte do projeto oralmente para o professor.
- Não gere blocos grandes de código de uma vez sem que eu tenha acompanhado o raciocínio.