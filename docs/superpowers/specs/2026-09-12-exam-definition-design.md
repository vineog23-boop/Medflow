# Catálogo de definições de exame

## Contexto

`ExamOrder` atualmente recebe `examCode` como texto livre. O Medflow precisa
de um catálogo interno para identificar quais exames o laboratório oferece e
impedir que novas ordens usem códigos inexistentes ou inativos.

Esta entrega permanece dentro do módulo `exam` do monólito. Não envolve
OpenFeign, RabbitMQ, API Gateway ou extração de microsserviço.

## Objetivo

Criar `ExamDefinition` como fonte das definições de exame disponíveis para
novas ordens, preservando o histórico quando uma definição for desativada.

## Modelo

`ExamDefinition` terá:

| Campo | Tipo | Regra |
| --- | --- | --- |
| `id` | `UUID` | Gerado pelo JPA. |
| `code` | `String` | Obrigatório, único, imutável, máximo de 50 caracteres. |
| `name` | `String` | Obrigatório, máximo de 150 caracteres. |
| `sampleType` | `SampleType` | Obrigatório e persistido como texto. |
| `active` | `boolean` | Inicia como `true`. |
| `createdAt` | `Instant` | Definido na criação e imutável. |
| `updatedAt` | `Instant` | Atualizado quando nome, amostra ou situação mudar. |
| `version` | `Long` | Controle otimista com `@Version`. |

Valores iniciais de `SampleType`:

```text
BLOOD
URINE
STOOL
SWAB
OTHER
```

## Invariantes

- `code` será normalizado com `trim()` e `toUpperCase(Locale.ROOT)`.
- `code` não poderá ser alterado após a criação.
- `name` será armazenado sem espaços externos.
- `name` vazio ou maior que 150 caracteres será rejeitado.
- `sampleType` nulo será rejeitado.
- Uma definição nova começará ativa.
- Atualização poderá alterar somente `name` e `sampleType`.
- Uma definição inativa poderá ter nome e tipo de amostra corrigidos, mas não
  poderá ser usada em novas ordens.
- Desativação será idempotente e não removerá o registro.
- Não haverá reativação nem exclusão física nesta entrega.

## Persistência

`ExamDefinitionRepository` estenderá
`JpaRepository<ExamDefinition, UUID>` e oferecerá
`boolean existsByCode(String code)` para impedir duplicidade antes da escrita.
A restrição única no banco será adicionada na etapa de PostgreSQL/Flyway; até
lá, a Service protegerá a regra no nível da aplicação.

## Aplicação e erros

`ExamDefinitionService` será responsável por:

- criar uma definição, rejeitando código já existente;
- buscar uma definição por UUID;
- listar definições de forma paginada;
- atualizar nome e tipo de amostra;
- desativar uma definição sem apagá-la.

Recurso inexistente produzirá `ExamDefinitionNotFoundException` e resposta
HTTP `404`. Código duplicado produzirá uma exceção de conflito e resposta HTTP
`409`. Os dois erros usarão `ProblemDetail` RFC 9457 pelo handler compartilhado.

## Contrato HTTP

| Método | Rota | Resultado |
| --- | --- | --- |
| `POST` | `/api/v1/exam-definitions` | Cria e responde `201`. |
| `GET` | `/api/v1/exam-definitions` | Lista paginada e responde `200`. |
| `GET` | `/api/v1/exam-definitions/{id}` | Busca por UUID e responde `200`. |
| `PUT` | `/api/v1/exam-definitions/{id}` | Atualiza nome e amostra e responde `200`. |
| `PATCH` | `/api/v1/exam-definitions/{id}/deactivation` | Desativa e responde `204`. |

Controllers receberão e retornarão DTOs; a entidade JPA não será exposta na
API.

## Integração futura com ordens

Depois deste CRUD, `ExamOrderService` passará a consultar o catálogo antes de
criar uma ordem. A criação será rejeitada quando o código não existir ou estiver
inativo. Essa integração será uma chamada Java local dentro do monólito e terá
uma especificação própria.

## Estratégia de implementação e testes

Por decisão do usuário, o código será escrito manualmente na sequência
`Entity -> Repository -> Service -> Controller`; os testes serão adicionados
depois do código de produção. Essa ordem não elimina a exigência de testes.

Antes de concluir a entrega, haverá:

- testes unitários das invariantes e comportamentos da entidade;
- testes unitários da Service com Repository simulado;
- testes web do Controller com MockMvc;
- execução completa de `./mvnw test` no módulo `medflow`.

O teste de integração da consulta derivada e da restrição única depende da
etapa PostgreSQL/Flyway e ficará explicitamente pendente até essa infraestrutura
existir.

## Fora de escopo

- validar `ExamOrder` contra o catálogo;
- configurar PostgreSQL ou criar migration;
- implementar autenticação e autorização;
- publicar eventos;
- usar OpenFeign, RabbitMQ ou API Gateway;
- extrair `order-service`.

## Arquivos previstos

- `medflow/src/main/java/br/com/medflow/exam/domain/ExamDefinition.java`
- `medflow/src/main/java/br/com/medflow/exam/domain/enums/SampleType.java`
- `medflow/src/main/java/br/com/medflow/exam/persistence/ExamDefinitionRepository.java`
- `medflow/src/main/java/br/com/medflow/exam/application/ExamDefinitionService.java`
- DTOs de entrada e saída em `exam/application/dto/`
- exceções em `exam/application/exception/`
- `medflow/src/main/java/br/com/medflow/exam/web/ExamDefinitionController.java`
- atualizações no handler compartilhado e seus testes
- testes de domínio, aplicação e web nos pacotes equivalentes de `src/test`
