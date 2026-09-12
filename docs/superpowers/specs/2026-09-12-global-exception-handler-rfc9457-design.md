# GlobalExceptionHandler com Problem Details RFC 9457

## Contexto

O Medflow usa Java 21, Spring Boot 4.1.1 e Spring MVC. Atualmente, exceções de
domínio e erros de entrada não possuem um contrato HTTP único. A implementação
adotará o suporte nativo do Spring a `ProblemDetail` e
`ResponseEntityExceptionHandler`, sem criar um DTO proprietário de erro.

Como o projeto não possui um domínio público para hospedar documentação dos
tipos de problema, cada tipo será identificado por uma URN estável. A RFC 9457
permite URIs não resolvíveis, embora recomende URLs documentáveis quando houver
um domínio sob controle da API.

## Objetivos

- Padronizar erros HTTP como `application/problem+json`.
- Preservar os membros RFC 9457: `type`, `title`, `status`, `detail` e
  `instance`.
- Diferenciar erros sintáticos, semânticos, de recurso inexistente e internos.
- Não expor stack traces, classes Java, SQL, credenciais ou detalhes internos.
- Aplicar o mesmo contrato aos módulos de pacientes e ordens de exame.
- Usar testes sem Mockito e sem mocks de Service ou Repository nesta mudança.

## Alternativas consideradas

### Suporte nativo do Spring - escolhido

Criar um `@RestControllerAdvice` que estende `ResponseEntityExceptionHandler` e
retorna `ProblemDetail`. Essa opção mantém integração com os erros internos do
Spring MVC, negociação de conteúdo e serialização `application/problem+json`.

### Apenas `spring.mvc.problemdetails.enabled=true`

Reduz código, porém oferece menos controle sobre mensagens em português,
extensões de validação e exceções específicas do domínio.

### DTO proprietário de erro

Oferece controle total, mas repetiria um padrão já suportado pelo framework e
dificultaria a interoperabilidade com clientes que conhecem RFC 9457.

## Organização

O handler ficará em `br.com.medflow.shared.web.error`, pois o contrato é
transversal e não pertence exclusivamente a pacientes ou exames.

Componentes previstos:

- `GlobalExceptionHandler`: converte exceções em `ProblemDetail`.
- `ValidationError`: representa cada item da extensão RFC `errors` com
  `detail` e `pointer`.
- `ProblemTypes`: centraliza as URNs para evitar strings divergentes.

## Contrato HTTP

| Situação | Status | Type | Title |
| --- | ---: | --- | --- |
| Paciente inexistente | 404 | `urn:medflow:problem:patient-not-found` | `Paciente não encontrado` |
| Ordem inexistente | 404 | `urn:medflow:problem:exam-order-not-found` | `Ordem de exame não encontrada` |
| JSON malformado ou parâmetro incompatível | 400 | `urn:medflow:problem:invalid-request` | `Requisição inválida` |
| Bean Validation | 422 | `urn:medflow:problem:validation-error` | `Dados inválidos` |
| Exceção inesperada | 500 | `urn:medflow:problem:internal-error` | `Erro interno` |

O `status` no corpo será sempre igual ao status HTTP. O `instance` será o path
da requisição. `title` será estável por tipo; `detail` explicará somente a
ocorrência e não será usado por clientes como código de erro.

### Validação

Erros de Bean Validation usarão a extensão `errors`, conforme o modelo da RFC
9457:

```json
{
  "type": "urn:medflow:problem:validation-error",
  "title": "Dados inválidos",
  "status": 422,
  "detail": "Um ou mais campos possuem valores inválidos.",
  "instance": "/patients",
  "errors": [
    {
      "detail": "Nome completo deve ser informado",
      "pointer": "#/fullName"
    }
  ]
}
```

Cada `pointer` será um fragmento JSON Pointer compatível com RFC 6901. Tokens
com `~` ou `/` serão escapados como `~0` e `~1`. A lista será ordenada pelo
pointer para manter respostas e testes determinísticos.

O `PatientDtoRequest` terá validação equivalente às invariantes expostas na
borda: nome obrigatório e limitado, CPF obrigatório e válido, e nascimento
obrigatório e não futuro. Os DTOs de exame manterão suas constraints atuais.

### Status HTTP

- `400 Bad Request`: o servidor não conseguiu interpretar corretamente o JSON
  ou converter um parâmetro da requisição.
- `404 Not Found`: o identificador é válido, mas o recurso não existe.
- `422 Unprocessable Content`: o JSON é sintaticamente correto, porém os valores
  violam constraints semânticas.
- `500 Internal Server Error`: falha inesperada. O detalhe público será genérico
  e a exceção completa ficará apenas no log do servidor.

## Fluxo

1. O Controller recebe e desserializa a requisição.
2. Bean Validation rejeita dados semanticamente inválidos antes da Service.
3. A Service pode lançar uma exceção de recurso não encontrado.
4. O `GlobalExceptionHandler` seleciona status e tipo, cria o `ProblemDetail` e
   adiciona extensões quando aplicável.
5. O Spring serializa o corpo como `application/problem+json`.

## Estratégia de testes sem mocks

Será criado um teste MVC do handler com um Controller mínimo definido apenas no
código de teste. Seus endpoints lançam exceções reais ou recebem um DTO real
com Bean Validation. O `GlobalExceptionHandler`, o DispatcherServlet, o
conversor JSON e o Validator serão reais.

Os cenários cobertos serão:

- paciente inexistente;
- ordem de exame inexistente;
- múltiplos erros de validação;
- JSON malformado;
- UUID inválido;
- erro inesperado sem vazamento da mensagem interna.

Essa estratégia não usa Mockito, Service mockada ou Repository mockado. Ela
valida o contrato HTTP do handler sem exigir PostgreSQL. Testes de integração
com Repository e banco real ficam fora deste escopo e exigirão uma estratégia
própria, preferencialmente Testcontainers.

## Arquivos previstos

- `medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java`
- `medflow/src/main/java/br/com/medflow/shared/web/error/ProblemTypes.java`
- `medflow/src/main/java/br/com/medflow/shared/web/error/ValidationError.java`
- `medflow/src/main/java/br/com/medflow/patient/application/dto/PatientDtoRequest.java`
- `medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java`

Nenhuma dependência ou migration será alterada.

## Referências

- RFC 9457, Problem Details for HTTP APIs: https://www.rfc-editor.org/rfc/rfc9457.html
- RFC 9110, HTTP Semantics: https://www.rfc-editor.org/rfc/rfc9110.html
- RFC 6901, JSON Pointer: https://www.rfc-editor.org/rfc/rfc6901.html
- Spring MVC Error Responses: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html

