# Organização interna do módulo `exam`

## Contexto

O módulo `exam` passou a reunir duas funcionalidades relacionadas, mas com
responsabilidades diferentes:

- `order`: registra e acompanha pedidos de exame;
- `definition`: mantém o catálogo de exames que podem ser solicitados.

A organização atual separa primeiro por camada técnica (`application`,
`domain`, `persistence` e `web`). Com a inclusão dos DTOs, exceções, Services e
Controllers de `ExamDefinition`, arquivos das duas funcionalidades passariam a
dividir os mesmos pacotes.

## Decisão

O módulo `exam` será organizado por funcionalidade e, dentro de cada uma, por
camada:

```text
br.com.medflow.exam
├── order
│   ├── application
│   │   ├── dto
│   │   └── exception
│   ├── domain
│   │   └── enums
│   ├── persistence
│   └── web
└── definition
    ├── application
    │   ├── dto
    │   └── exception
    ├── domain
    │   └── enums
    ├── persistence
    └── web
```

Os testes devem espelhar essa estrutura em `src/test/java`.

## Responsabilidades

### `exam.order`

- entidade `ExamOrder` e seus estados;
- criação, consulta, atualização e evolução do pedido;
- DTOs e exceções específicos da ordem;
- persistência e endpoints de ordens.

### `exam.definition`

- entidade `ExamDefinition` e `SampleType`;
- manutenção do catálogo de exames;
- criação, consulta, atualização e desativação de definições;
- DTOs e exceções específicos da definição;
- persistência e endpoints do catálogo.

## Dependências permitidas

- `web` depende de `application`.
- `application` coordena os casos de uso e depende de `domain` e
  `persistence` da própria funcionalidade.
- `persistence` conhece a entidade de `domain` que persiste.
- `domain` não depende de `application`, `persistence` ou `web`.
- A futura validação de uma ordem contra o catálogo seguirá a direção
  `order.application -> definition.application`.
- `order.application` não acessará `definition.persistence` diretamente.
- O tratamento HTTP comum continuará em `br.com.medflow.shared.web.error`.

Essa direção mantém o catálogo independente das ordens e evita um ciclo entre
as duas funcionalidades.

## Convenções

- DTOs permanecem em `application.dto`; não serão criados subpacotes
  adicionais `request` e `response` neste momento.
- Exceções de casos de uso permanecem em `application.exception` da
  funcionalidade correspondente.
- Os nomes das classes continuarão explícitos, como
  `ExamOrderNotFoundException` e `ExamDefinitionNotFoundException`.
- Não será criado um pacote `shared` dentro de `exam` até existir uma abstração
  realmente utilizada pelas duas funcionalidades.

## Migração dos arquivos existentes

1. Mover os arquivos de ordem para `br.com.medflow.exam.order`.
2. Mover os arquivos de definição para `br.com.medflow.exam.definition`.
3. Atualizar declarações de pacote e imports da produção e dos testes.
4. Executar a suíte completa antes de continuar o desenvolvimento da Service e
   do Controller de `ExamDefinition`.

A mudança reorganiza pacotes Java; ela não altera rotas HTTP, payloads,
entidades, tabelas ou regras de negócio.

## Impacto na documentação existente

Esta especificação substitui somente os caminhos de pacote descritos em:

- `docs/superpowers/specs/2026-09-12-exam-definition-design.md`;
- `docs/superpowers/plans/2026-09-12-exam-definition.md`.

Os requisitos funcionais e a sequência de desenvolvimento desses documentos
continuam válidos. O plano de implementação deverá ser atualizado com os novos
caminhos antes da movimentação dos arquivos.

## Validação

- Conferir que nenhuma classe de ordem permaneceu nos pacotes antigos de
  `exam`.
- Conferir que nenhuma classe de definição permaneceu nos pacotes antigos.
- Executar `./mvnw test` dentro do módulo `medflow`.
- Preservar as classes ainda em desenvolvimento e não incluir `output/` ou
  `tmp/` em commits.
