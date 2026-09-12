# Arquitetura distribuída e domínio do Medflow

## Contexto

O Medflow é uma plataforma de rastreabilidade de exames laboratoriais. O
projeto atual é um monólito modular em Java 21 e Spring Boot 4.1.1, com os
módulos de pacientes e ordens de exame. Esta especificação estabelece o
domínio, as fronteiras de serviço e a evolução gradual para uma arquitetura
distribuída, sem antecipar infraestrutura antes de haver regras de negócio
testadas.

O escopo inicial é um único laboratório. Não há diagnóstico médico,
prescrição, prontuário clínico ou uso de dados reais de pacientes.

## Objetivos

- Rastrear um exame do pedido à liberação do resultado.
- Permitir que o paciente autenticado consulte exclusivamente os próprios
  resultados liberados.
- Separar dados, responsabilidades e implantação dos serviços sem criar
  dependências diretas entre bancos.
- Usar HTTP síncrono apenas para leitura e validação; usar eventos para efeitos
  assíncronos e propagação de estado.
- Preparar o projeto para Spring Security, PostgreSQL, Flyway, RabbitMQ,
  OpenFeign, Docker Compose e testes de integração.

## Alternativas consideradas

### Monólito modular com extração progressiva - escolhida

As regras serão consolidadas e testadas no módulo atual antes de cada extração.
Quando uma fronteira estiver estável, ela se tornará um serviço com banco e
contrato próprios. É a opção com melhor relação entre aprendizado, velocidade
e risco operacional.

### Microsserviços completos desde o início

Ensina infraestrutura cedo, mas amplia de imediato as dificuldades de
debugging, autenticação, consistência distribuída, deploy e testes. Não é a
opção inicial.

### Monólito único permanente

É simples para o estágio atual, mas não exercita comunicação interna,
mensageria e contratos distribuídos, que são objetivos explícitos do projeto.

## Domínio e papéis

| Papel | Responsabilidades |
| --- | --- |
| `LAB_OPERATOR` | Cadastrar paciente e pedido; registrar coleta e recebimento. |
| `LAB_ANALYST` | Processar amostra e registrar resultado técnico. |
| `LAB_SUPERVISOR` | Validar e liberar resultado. |
| `PATIENT` | Consultar somente resultados próprios já liberados. |

O fluxo central é:

```text
Paciente -> Ordem de exame -> Amostra -> Resultado -> Notificação
```

`ExamOrder` representa a solicitação e seu estado macro. `Sample` representa
o item físico rastreável. `ExamResult` representa o resultado técnico, que só
fica disponível ao paciente após validação e liberação. `Notification` registra
a tentativa de comunicação; não carrega nem persiste o resultado clínico.

## Serviços-alvo e responsabilidades

Os itens desta seção representam as fronteiras de implantação desejadas após
as extrações. Durante a fase atual, `patient`, `exam`, `laboratory`,
`notification` e identidade são módulos internos da mesma aplicação Spring
Boot e compartilham uma única unidade de implantação.

| Serviço | Dados próprios | Responsabilidade |
| --- | --- | --- |
| `api-gateway` | nenhum | Entrada pública, roteamento, validação inicial de JWT e `correlationId`. |
| `identity-service` | `UserAccount`, `Role` | OAuth 2.1/OpenID Connect e emissão de tokens. |
| `patient-service` | `Patient` | Cadastro e vínculo entre paciente e o `subject` da identidade. |
| `order-service` | `ExamOrder`, `ExamDefinition` | Catálogo, pedido e estado macro do exame. |
| `laboratory-service` | `Sample`, `SampleMovement`, `ExamResult` | Rastreabilidade física, processamento, validação e liberação. |
| `notification-service` | `Notification` | Consumo de liberação e registro/envio de notificação genérica. |

`Sample` e `ExamResult` permanecem no mesmo serviço para que o ciclo físico e
a liberação técnica não dependam de chamadas remotas em cada transição.

Durante o monólito haverá um único banco PostgreSQL, com ownership lógico das
tabelas por módulo. Após a extração, cada serviço será dono exclusivo de seu
banco; não haverá leitura direta, joins nem chaves estrangeiras entre bancos
de serviços distintos.

## Segurança

Na fase de monólito, autenticação e autorização serão implementadas em um
módulo interno de identidade, dentro da aplicação `medflow`. Esse módulo será
responsável por usuários, papéis, emissão de tokens e configuração do Resource
Server, mas ainda não será implantado de forma independente.

O módulo usará Spring Authorization Server; JWT não será criado manualmente.
Quando houver extração, ele poderá se tornar o `identity-service`. Nessa fase,
o gateway e cada serviço de domínio serão Resource Servers e validarão
assinatura, emissor, expiração e autoridades do token.

O JWT conterá o `sub` e as autoridades necessárias. Durante o monólito, o
módulo `patient` será a fonte do vínculo entre `Patient.id` e o `sub`; após a
extração, essa responsabilidade será do `patient-service`. Ao consultar
resultado, o módulo `laboratory` — ou o futuro `laboratory-service` — deve
garantir que o paciente autenticado é o titular e que o resultado está em
`RELEASED`. Isso impede acesso indevido por alteração de identificadores na
URL.

Chamadas serviço a serviço usarão credencial própria, não token ou senha do
paciente. Eventos e logs não conterão dados pessoais identificáveis nem valor
de resultado.

## Estados e regras de negócio

Estados macro da ordem:

```text
RECEIVED -> AWAITING_COLLECTION -> COLLECTED -> IN_TRANSIT
-> RECEIVED_AT_LAB -> PROCESSING -> VALIDATED -> RELEASED
```

`REJECTED` e `CANCELLED` são terminais. Estados físicos da amostra incluem
`COLLECTED`, `IN_TRANSIT`, `RECEIVED_AT_LAB`, `PROCESSING` e `REJECTED`.
`SampleMovement` é um histórico imutável de movimentações.

`ExamResult` evolui de `DRAFT` para `VALIDATED` e então `RELEASED`. Somente
`LAB_SUPERVISOR` pode liberar um resultado previamente validado. Um resultado
rejeitado, cancelado ou ainda não liberado não é visível ao paciente.

## Comunicação e consistência

Durante o monólito, módulos colaboram em processo por interfaces Java; não há
OpenFeign entre pacotes da mesma aplicação. Após as extrações, chamadas HTTP
internas serão limitadas a leitura e validação, sem escrita distribuída:

- `order-service` consulta `patient-service` para validar o paciente antes de
  criar uma ordem;
- `laboratory-service` consulta `order-service` para validar uma ordem durante
  a coleta e consulta `patient-service` para resolver titularidade em consultas
  autorizadas.

Quando surgir a primeira fronteira síncrona remota, o projeto usará OpenFeign
como adaptador HTTP interno por ser um objetivo de aprendizado. O adaptador
ficará atrás de uma porta da aplicação, em `client/`, para permitir migração
futura para Spring HTTP Service Clients sem alterar os casos de uso. A
documentação do Spring classifica Spring Cloud OpenFeign como feature-complete
e recomenda HTTP Service Clients para cenários novos.

Fluxos que produzem efeitos em outros serviços usarão RabbitMQ e transactional
outbox:

1. A criação da ordem grava a ordem e uma mensagem `exam-order.created.v1` na
   mesma transação local.
2. Coleta, movimentações e liberação de resultado gravam a alteração local e
   o evento correspondente na outbox.
3. Um publicador entrega os eventos ao RabbitMQ.
4. Consumidores atualizam projeções próprias, como o estado macro da ordem e
   a notificação ao paciente.

Todo evento terá `eventId`, `eventType`, `version`, `occurredAt` e
`correlationId`, além dos IDs estritamente necessários. Consumidores devem ser
idempotentes por `eventId`. Filas terão confirmação manual, retry limitado e
dead-letter queue. A notificação enviada ao paciente será genérica e não
exporá o resultado.

## Estrutura do repositório

O estágio atual permanecerá um monólito modular em um único módulo Maven:

```text
Medflow/
├── medflow/
│   ├── pom.xml
│   └── src/main/java/br/com/medflow/
│       ├── patient/
│       ├── exam/
│       ├── laboratory/
│       ├── notification/
│       ├── workflow/
│       └── shared/
└── docs/superpowers/
```

Os módulos `patient` e `exam` são os primeiros já implementados. `exam` é o
precursor do futuro `order-service`; `laboratory` e `notification` serão
preenchidos no mesmo monólito antes de serem candidatos à extração.

Quando uma fronteira tiver regras, testes e contratos estáveis, o repositório
evoluirá para um monorepo Maven multi-módulo:

```text
Medflow/
├── apps/
│   └── medflow-monolith/       # aplicação existente durante a transição
├── services/
│   ├── patient-service/        # criado somente na extração correspondente
│   ├── order-service/
│   ├── laboratory-service/
│   └── notification-service/
├── libs/
│   └── event-contracts/
├── infra/
├── docs/
└── pom.xml                     # agregador Maven
```

`event-contracts` conterá somente envelopes e contratos versionados de
mensagens. É proibido compartilhar entidades JPA, repositórios, exceções ou
regras de negócio nesse módulo.

Dentro de cada módulo de domínio, a organização será:

```text
domain/        entidades, value objects e regras
application/   casos de uso e portas
persistence/   JPA, repositórios e adaptadores de banco
messaging/     outbox, publicadores e consumidores
client/        adaptadores OpenFeign
web/           controllers, DTOs e handlers HTTP
config/         configuração técnica
```

Essa organização preserva o percurso didático `Entity -> Repository ->
Service/Application -> Controller`, mas separa regras de domínio, adaptadores
e contratos de API.

## Operação

O Docker Compose será introduzido incrementalmente: PostgreSQL na etapa de
persistência e RabbitMQ na etapa de mensageria. Gateway e containers de
serviços serão adicionados apenas quando começar a extração. Durante o
monólito, a aplicação terá migrations Flyway em
`src/main/resources/db/migration`; depois, cada serviço extraído terá suas
próprias migrations. Senhas, URIs e demais segredos serão fornecidos por
variáveis de ambiente, nunca versionados.

No monólito, um filtro HTTP gerará o `correlationId`; após sua introdução, o
gateway passará a criá-lo ou propagá-lo para HTTP e eventos. A aplicação e os
futuros serviços exporão health checks por Actuator. A observabilidade inicial
cobrirá logs estruturados, identificador de correlação e saúde; tracing
distribuído completo pode ser adicionado após os fluxos críticos estarem
estáveis.

## Contratos externos e erros

APIs públicas serão versionadas sob `/api/v1`. Controllers recebem DTOs e não
expõem entidades JPA. Entrada será validada com Bean Validation e os erros
seguirão RFC 9457, conforme o handler global já existente.

Identificadores serão UUIDs. Dados clínicos, PII, SQL, stack traces e
credenciais não serão retornados em respostas de erro nem registrados em logs.

## Estratégia de testes

| Camada | Estratégia | Cobertura principal |
| --- | --- | --- |
| Domínio | Unitário | Invariantes e transições de estado. |
| Aplicação | Unitário com mocks das portas | Regras e orquestração. |
| Web | Integração leve | HTTP, DTO, validação, autorização e erros. |
| Persistência | Integração | JPA, migrations e consultas reais. |
| Mensageria | Integração | Publicação, consumo, idempotência e DLQ. |
| Serviço | Testcontainers | PostgreSQL e RabbitMQ nos fluxos críticos. |

Todo comportamento novo começará por um teste que falha. A implementação
avançará da regra testada para entidade, repositório, aplicação e controller.

## Roadmap de entregas

1. Consolidar `Patient` e `ExamOrder`: completar o teste web de ordens,
   versionar os endpoints sob `/api/v1` e manter a suíte atual verde.
2. Adicionar PostgreSQL, Flyway e testes com Testcontainers.
3. Implementar `ExamDefinition` e validar a existência de paciente e exame ao
   criar uma ordem.
4. Implementar identidade e Spring Security dentro do monólito, com JWT e
   papéis; após existir resultado, proteger também a consulta do paciente.
5. Implementar laboratório: amostra, movimentação, resultado, validação e
   liberação.
6. Implementar `Notification` e adicionar RabbitMQ, outbox, idempotência,
   retry e DLQ.
7. Verificar prontidão para extração: regras e contratos estáveis, testes de
   integração, ownership de dados e observabilidade mínima.
8. Reorganizar para o monorepo Maven multi-módulo e extrair primeiro o
   `notification-service`, que se comunica por eventos e tem baixo acoplamento.
9. Extrair as demais fronteiras uma por vez. Introduzir OpenFeign apenas nas
   leituras e validações realmente remotas, com timeout, autenticação de
   serviço e circuit breaker; adicionar gateway quando houver mais de uma
   aplicação exposta.

## Fora de escopo inicial

- Diagnóstico, prescrição e prontuário médico.
- Integração com hospitais, convênios ou laboratórios externos.
- Dados reais de pacientes.
- Kafka, analytics e replay de eventos.
- Descoberta de serviços dedicada; no ambiente local, endereçamento será feito
  por configuração e DNS do Docker Compose.

## Referências

- Spring Cloud OpenFeign: https://docs.spring.io/spring-cloud-openfeign/reference/index.html
- Spring Authorization Server: https://docs.spring.io/spring-authorization-server/reference/overview.html
- Spring Security OAuth2 Resource Server JWT: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- RFC 9457, Problem Details for HTTP APIs: https://www.rfc-editor.org/rfc/rfc9457.html
