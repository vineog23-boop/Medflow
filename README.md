<h1 align="center">🩺 Medflow</h1>
<p align="center">Pacientes e ordens de exame com domínio, contratos HTTP e testes.</p>
<p align="center">
  <img src="https://img.shields.io/badge/Java-21-2563EB?style=flat-square" alt="Java: 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-0F766E?style=flat-square" alt="Spring Boot: 4.1.1">
  <img src="https://img.shields.io/badge/Banco-PostgreSQL%20pendente-475569?style=flat-square" alt="Banco: PostgreSQL pendente">
  <img src="https://img.shields.io/badge/Status-Em%20desenvolvimento-475569?style=flat-square" alt="Status: Em desenvolvimento">
</p>

<p align="center"><a href="#visão-geral">Visão geral</a> · <a href="#funcionalidades">Funcionalidades</a> · <a href="#como-rodar">Execução</a> · <a href="#testes">Testes</a></p>

---

## Visão geral

Medflow é uma API REST para apoiar o fluxo de pacientes e ordens de exames. O checkpoint atual implementa o CRUD desses dois recursos, com contratos HTTP validados, persistência por JPA e respostas de erro padronizadas em RFC 9457.

## Funcionalidades

- ✅ Cadastro, consulta paginada, busca por ID, atualização e remoção de pacientes;
- ✅ Validação de nome, CPF e data de nascimento do paciente;
- ✅ Cadastro, consulta paginada, busca por ID, atualização e remoção de ordens de exame;
- ✅ Prioridade `NORMAL` ou `URGENT` e status inicial `RECEIVED` para ordens de exame;
- ✅ DTOs na borda da API, sem expor entidades JPA diretamente;
- ✅ Erros HTTP padronizados para validação, UUID inválido, recurso não encontrado e falhas inesperadas;
- ✅ Testes unitários de domínio, serviços, controllers e tratamento global de erros;
- 🚧 Configuração versionada de PostgreSQL e migrations Flyway;
- 🚧 Autenticação e autorização com Spring Security;
- 🚧 Mensageria com RabbitMQ;
- 🚧 Docker e documentação OpenAPI/Swagger.

## Tecnologias

| Tecnologia | Versão / uso | Descrição |
| --- | --- | --- |
| Java | 21 | Linguagem da aplicação. |
| Spring Boot | 4.1.1 | Base da aplicação e configuração do ecossistema Spring. |
| Spring Web MVC | Spring Boot Starter | API HTTP REST. |
| Spring Data JPA | Spring Boot Starter | Persistência e repositórios. |
| Hibernate | Via Spring Data JPA | Mapeamento objeto-relacional. |
| Bean Validation | Spring Boot Starter | Validação dos contratos de entrada. |
| PostgreSQL | Driver runtime | Banco relacional previsto para a aplicação. |
| Flyway | Spring Boot Starter | Versionamento de schema, ainda sem migrations no repositório. |
| Spring Security | Spring Boot Starter | Dependência preparada; regras de segurança ainda não configuradas. |
| RabbitMQ / AMQP | Spring Boot Starter | Dependência preparada; fluxos de mensageria ainda não implementados. |
| Maven Wrapper | Incluído | Build e execução sem instalação global do Maven. |

## Como rodar

### Estado atual da infraestrutura

O repositório ainda não contém migrations Flyway, configuração de `DataSource` nem `Dockerfile`/`compose.yaml`. Portanto, a API não possui uma execução local completa e reproduzível neste checkpoint. Esses artefatos devem ser adicionados antes de apontar a aplicação para PostgreSQL ou publicar instruções de containerização.

### Preparar o projeto

Com JDK 21 instalado, clone o repositório e entre no módulo Maven:

```bash
git clone https://github.com/vineog23-boop/Medflow.git
cd Medflow/medflow
```

Execute os testes sem depender de um banco externo:

```bash
bash ./mvnw test
```

No Windows: `.\mvnw.cmd test`.

O teste de contexto exclui as autoconfigurações de persistência e usa repositórios simulados. Os testes não comprovam uma conexão PostgreSQL funcionando.

**Somente após configurar datasource, migrations e acesso HTTP**, inicie a aplicação com `bash ./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Com um datasource válido configurado, a aplicação usará a porta padrão `8080`.

### Com Docker

Ainda não há suporte a Docker neste checkpoint. Quando o `compose.yaml` estiver disponível, esta seção deverá conter os serviços, variáveis de ambiente e o comando de inicialização correspondentes.

## Endpoints

Rotas implementadas nos controllers. Os exemplos abaixo descrevem os contratos e dependem da infraestrutura e das regras de acesso configuradas para uso real. Todos os dados dos exemplos são fictícios.

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/patients` | Lista pacientes de forma paginada. Aceita `page`, `size` e `sort`. |
| `GET` | `/patients/{id}` | Busca um paciente pelo UUID. |
| `POST` | `/patients` | Cadastra um paciente. |
| `PUT` | `/patients/{id}` | Atualiza um paciente. |
| `DELETE` | `/patients/{id}` | Remove um paciente. |
| `GET` | `/exam-orders` | Lista ordens de exame de forma paginada. Aceita `page`, `size` e `sort`. |
| `GET` | `/exam-orders/{id}` | Busca uma ordem de exame pelo UUID. |
| `POST` | `/exam-orders` | Cria uma ordem de exame. |
| `PUT` | `/exam-orders/{id}` | Atualiza uma ordem de exame. |
| `DELETE` | `/exam-orders/{id}` | Remove uma ordem de exame. |

### Cadastrar paciente

```bash
curl -X POST http://localhost:8080/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Maria da Silva",
    "cpf": "52998224725",
    "birthDate": "1990-05-20"
  }'
```

Resposta esperada (`201 Created`):

```json
{
  "id": "uuid-gerado-pela-aplicacao",
  "fullName": "Maria da Silva",
  "cpf": "52998224725",
  "birthDate": "1990-05-20",
  "createdAt": "2026-09-12T12:00:00Z"
}
```

### Criar ordem de exame

Substitua o `patientId` pelo UUID de um paciente já cadastrado.

```bash
curl -X POST http://localhost:8080/exam-orders \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "00000000-0000-0000-0000-000000000001",
    "examCode": "HEMOGRAMA",
    "priority": "URGENT"
  }'
```

Resposta esperada (`201 Created`):

```json
{
  "id": "uuid-gerado-pela-aplicacao",
  "patientId": "00000000-0000-0000-0000-000000000001",
  "examCode": "HEMOGRAMA",
  "priority": "URGENT",
  "status": "RECEIVED",
  "createdAt": "2026-09-12T12:00:00Z",
  "updatedAt": "2026-09-12T12:00:00Z"
}
```

## Tratamento de erros

A API usa `application/problem+json` e `ProblemDetail` para retornar erros. Por exemplo, um corpo inválido resulta em `422 Unprocessable Content` com o tipo `urn:medflow:problem:validation-error` e a lista de campos inválidos.

```json
{
  "type": "urn:medflow:problem:validation-error",
  "title": "Dados inválidos",
  "status": 422,
  "detail": "Um ou mais campos estão inválidos.",
  "instance": "/patients",
  "errors": [
    {
      "message": "Nome completo deve ser informado",
      "pointer": "#/fullName"
    }
  ]
}
```

## Arquitetura

O projeto é organizado por funcionalidade. Cada módulo concentra domínio, aplicação, persistência e borda HTTP do seu contexto, enquanto os contratos de erro compartilhados ficam em `shared`.

```text
medflow/
├── src/main/java/br/com/medflow/
│   ├── MedflowApplication.java             # Inicialização da aplicação Spring Boot
│   ├── patient/
│   │   ├── application/                    # Casos de uso, DTOs e exceções de pacientes
│   │   ├── domain/                         # Entidade e regras de domínio de pacientes
│   │   ├── persistence/                    # Repositório JPA de pacientes
│   │   └── web/                            # Controller HTTP de pacientes
│   ├── exam/
│   │   ├── application/                    # Casos de uso, DTOs e exceções de ordens
│   │   ├── domain/                         # Entidade, status e prioridade de exames
│   │   ├── persistence/                    # Repositório JPA de ordens de exame
│   │   └── web/                            # Controller HTTP de ordens de exame
│   └── shared/web/error/                   # RFC 9457, tipos de problema e handler global
└── src/test/java/br/com/medflow/            # Testes unitários e de camada web
```

## Testes

Os testes cobrem regras das entidades, serviços, controllers e a tradução de exceções para o contrato HTTP. Execute a suíte a partir de `medflow/`:

```bash
bash ./mvnw test
```

No Windows: `.\mvnw.cmd test`. A cobertura HTTP existente inclui o controller de pacientes e o handler de erros; ampliar os testes do controller de ordens é uma próxima melhoria.

## Decisões e desafios técnicos

- **Organização por funcionalidade:** `patient` e `exam` agrupam as responsabilidades do respectivo contexto, evitando pacotes globais por camada que misturam regras de negócio distintas.
- **DTOs como contrato de borda:** controllers recebem e retornam `record`s específicos; as entidades JPA não se tornam parte do contrato público da API.
- **Validação antes da regra de negócio:** Bean Validation protege os contratos HTTP, enquanto as entidades também validam seus invariantes para não dependerem exclusivamente da camada web.
- **Concorrência otimista:** as entidades possuem `@Version`, permitindo detectar atualizações concorrentes quando a persistência estiver configurada.
- **Erros previsíveis:** `GlobalExceptionHandler` centraliza falhas esperadas e inesperadas no formato RFC 9457, preservando controllers focados no fluxo HTTP normal.
- **Infraestrutura pendente explicitamente:** PostgreSQL, Flyway, segurança e AMQP aparecem no `pom.xml`, mas só serão documentados como recursos utilizáveis quando houver configuração e fluxos correspondentes no código.

## Roadmap

1. Adicionar configuração de PostgreSQL e migrations Flyway para `patients` e `exam_orders`.
2. Configurar autenticação e autorização com Spring Security.
3. Definir os eventos de mensageria e integrar RabbitMQ quando o caso de uso estiver estabelecido.
4. Adicionar Docker e documentação OpenAPI/Swagger após a infraestrutura estar funcional.

## Autor

**Vinícius Oliveira** · [GitHub](https://github.com/vineog23-boop) · [LinkedIn](https://www.linkedin.com/in/vinícius-oliveira-1770b7306)

## Licença

Este projeto ainda não possui uma licença de uso definida.
