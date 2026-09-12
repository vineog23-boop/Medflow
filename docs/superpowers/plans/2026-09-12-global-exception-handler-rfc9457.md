# GlobalExceptionHandler RFC 9457 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Padronizar os erros HTTP de pacientes, ordens de exame, validação e falhas inesperadas com `ProblemDetail` RFC 9457.

**Architecture:** Um `@RestControllerAdvice` compartilhado estenderá `ResponseEntityExceptionHandler`, converterá exceções de domínio e do Spring MVC para `ProblemDetail` e manterá as URNs em uma classe central. Os testes usarão Spring MVC, Validator e exceções reais com um Controller exclusivo de teste, sem Mockito e sem mocks de Service ou Repository.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Framework 7.0.9, Bean Validation, Hibernate Validator 9.1.3, JUnit 5 e MockMvc.

**Spec:** `docs/superpowers/specs/2026-09-12-global-exception-handler-rfc9457-design.md`

## Global Constraints

- Serializar erros como `application/problem+json`.
- Preservar `type`, `title`, `status`, `detail` e `instance` da RFC 9457.
- Usar URNs `urn:medflow:problem:*`; não inventar domínio público.
- Usar `400` para entrada malformada, `404` para recurso inexistente, `422` para validação semântica e `500` para falha inesperada.
- Não expor stack trace, nomes de classes, SQL, credenciais ou a mensagem de exceções inesperadas.
- Não adicionar dependências nem alterar migrations.
- Não criar novos testes com Mockito ou mocks de Service e Repository.
- Preservar alterações não commitadas já existentes no workspace.

## Execution Override

- Em 12/09/2026, o usuário optou explicitamente por implementar primeiro e
  escrever os testes depois. Portanto, as etapas RED deste plano não serão
  executadas como TDD; os mesmos comportamentos serão cobertos após o código de
  produção estar pronto.
- Os novos testes continuarão sem Mockito e sem mocks de Service ou Repository.
- A implementação será feita diretamente na branch `main`, com autorização
  explícita do usuário, preservando as alterações locais existentes.

---

## File Structure

- `ProblemTypes.java`: única fonte das URNs do contrato.
- `ValidationError.java`: item imutável da extensão `errors`.
- `GlobalExceptionHandler.java`: tradução de exceções para respostas HTTP.
- `PatientDtoRequest.java`: constraints da borda HTTP para pacientes.
- `GlobalExceptionHandlerTest.java`: contrato HTTP real do handler sem Mockito.

### Task 1: Tipos de problema e respostas 404

**Files:**
- Create: `medflow/src/main/java/br/com/medflow/shared/web/error/ProblemTypes.java`
- Create: `medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java`
- Test: `medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: `PatientNotFoundException`, `ExamOrderNotFoundException`, `ProblemDetail`.
- Produces: `ProblemTypes` com as duas URNs de recurso inexistente; handlers públicos para as duas exceções de domínio.

- [ ] **Step 1: Write the failing 404 tests**

Criar `GlobalExceptionHandlerTest` com `MockMvcBuilders.standaloneSetup(new ErrorProbeController())`, `setControllerAdvice(new GlobalExceptionHandler())` e um Controller aninhado:

```java
@RestController
@RequestMapping("/test-errors")
static class ErrorProbeController {
    @GetMapping("/patients/{id}")
    void patientNotFound(@PathVariable UUID id) {
        throw new PatientNotFoundException(id);
    }

    @GetMapping("/exam-orders/{id}")
    void examOrderNotFound(@PathVariable UUID id) {
        throw new ExamOrderNotFoundException(id);
    }
}
```

Adicionar dois testes que façam requisições com o UUID literal
`76cb605b-71dc-47e8-83b5-83f2ef99a7cf` e verifiquem:

```java
.andExpect(status().isNotFound())
.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
.andExpect(jsonPath("$.type").value("urn:medflow:problem:patient-not-found"))
.andExpect(jsonPath("$.title").value("Paciente não encontrado"))
.andExpect(jsonPath("$.status").value(404))
.andExpect(jsonPath("$.detail").value(
        "Paciente com id: 76cb605b-71dc-47e8-83b5-83f2ef99a7cf não encontrado"))
.andExpect(jsonPath("$.instance").value(
        "/test-errors/patients/76cb605b-71dc-47e8-83b5-83f2ef99a7cf"));
```

O segundo teste usa `exam-order-not-found`, título
`Ordem de exame não encontrada` e a mensagem atual da exceção de exame.

- [ ] **Step 2: Run the tests and verify RED**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: FAIL na compilação porque `GlobalExceptionHandler` ainda não existe.

- [ ] **Step 3: Implement the problem types and 404 handlers**

Criar `ProblemTypes` como classe utilitária não instanciável:

```java
public final class ProblemTypes {
    public static final URI PATIENT_NOT_FOUND = URI.create("urn:medflow:problem:patient-not-found");
    public static final URI EXAM_ORDER_NOT_FOUND = URI.create("urn:medflow:problem:exam-order-not-found");

    private ProblemTypes() {
    }
}
```

Criar `GlobalExceptionHandler extends ResponseEntityExceptionHandler` anotado
com `@RestControllerAdvice`. Os dois métodos `@ExceptionHandler` devem chamar um
helper que constrói `ProblemDetail.forStatusAndDetail`, define `type`, `title` e
`instance`, e retorna `ResponseEntity` com `MediaType.APPLICATION_PROBLEM_JSON`.

Assinaturas:

```java
@ExceptionHandler(PatientNotFoundException.class)
public ResponseEntity<ProblemDetail> handlePatientNotFound(
        PatientNotFoundException exception,
        HttpServletRequest request)

@ExceptionHandler(ExamOrderNotFoundException.class)
public ResponseEntity<ProblemDetail> handleExamOrderNotFound(
        ExamOrderNotFoundException exception,
        HttpServletRequest request)
```

- [ ] **Step 4: Run the 404 tests and verify GREEN**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: os dois testes passam.

- [ ] **Step 5: Commit the 404 contract**

```powershell
git add medflow/src/main/java/br/com/medflow/shared/web/error medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java
git commit -m "feat(error): padroniza recursos inexistentes com ProblemDetail"
```

### Task 2: Validação semântica com 422

**Files:**
- Create: `medflow/src/main/java/br/com/medflow/shared/web/error/ValidationError.java`
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/ProblemTypes.java`
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java`
- Modify: `medflow/src/main/java/br/com/medflow/patient/application/dto/PatientDtoRequest.java`
- Modify: `medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: `MethodArgumentNotValidException`, `PatientDtoRequest`.
- Produces: `422 ProblemDetail` com `List<ValidationError>` na propriedade `errors`.

- [ ] **Step 1: Write the failing validation test**

Inicializar um Validator real no `MockMvc`:

```java
LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
validator.afterPropertiesSet();
mockMvc = standaloneSetup(new ErrorProbeController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .build();
```

Adicionar ao Controller de teste:

```java
@PostMapping("/validation")
void validation(@Valid @RequestBody PatientDtoRequest request) {
}
```

Enviar nome vazio, CPF `123` e data `2999-01-01`. Verificar status `422`, type
`validation-error`, título `Dados inválidos`, instance
`/test-errors/validation` e a lista ordenada:

```json
[
  {"detail":"Data de nascimento não pode estar no futuro","pointer":"#/birthDate"},
  {"detail":"CPF deve ser válido","pointer":"#/cpf"},
  {"detail":"Nome completo deve ser informado","pointer":"#/fullName"}
]
```

- [ ] **Step 2: Run the validation test and verify RED**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: FAIL porque as constraints do paciente e o override de validação ainda
não produzem o contrato `422`.

- [ ] **Step 3: Complete PatientDtoRequest constraints**

Preservar `@CPF` e `@NotBlank` já existentes e completar:

```java
public record PatientDtoRequest(
        @NotBlank(message = "Nome completo deve ser informado")
        @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres")
        String fullName,

        @NotBlank(message = "CPF deve ser informado")
        @CPF(message = "CPF deve ser válido")
        String cpf,

        @NotNull(message = "Data de nascimento deve ser informada")
        @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
        LocalDate birthDate
) {
}
```

- [ ] **Step 4: Implement validation mapping**

Adicionar a `ProblemTypes` somente nesta etapa:

```java
public static final URI VALIDATION_ERROR =
        URI.create("urn:medflow:problem:validation-error");
```

Criar o item imutável da extensão `errors`:

```java
public record ValidationError(String detail, String pointer) {
}
```

Sobrescrever:

```java
@Override
protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request)
```

Mapear `FieldError` para `ValidationError`, usar fallback `Valor inválido` quando
`defaultMessage` for nula, ordenar por `pointer`, definir a propriedade
`errors`, e responder com `HttpStatus.UNPROCESSABLE_CONTENT`, nome adotado pelo
Spring 7 para o status HTTP `422`.

Criar `toJsonPointer(String field)` separando propriedades aninhadas por ponto
e escapando cada token nesta ordem:

```java
private String escapeJsonPointerToken(String token) {
    return token.replace("~", "~0").replace("/", "~1");
}
```

- [ ] **Step 5: Run the validation test and verify GREEN**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: testes 404 e 422 passam.

- [ ] **Step 6: Commit validation handling**

```powershell
git add medflow/src/main/java/br/com/medflow/patient/application/dto/PatientDtoRequest.java medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java
git commit -m "feat(error): retorna detalhes de validacao RFC 9457"
```

### Task 3: Requisições malformadas e tipos incompatíveis

**Files:**
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/ProblemTypes.java`
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java`
- Modify: `medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: `HttpMessageNotReadableException`, `TypeMismatchException`.
- Produces: `400 ProblemDetail` com type `invalid-request`.

- [ ] **Step 1: Write failing invalid-request tests**

Adicionar ao Controller de teste:

```java
@PostMapping("/body")
void body(@RequestBody PatientDtoRequest request) {
}

@GetMapping("/uuid/{id}")
void uuid(@PathVariable UUID id) {
}
```

Testar POST com corpo `{` e GET com `/test-errors/uuid/uuid-invalido`.
Verificar `400`, `application/problem+json`, type `invalid-request`, título
`Requisição inválida`, o path em `instance` e detalhes públicos:

- JSON: `O corpo da requisição contém JSON inválido ou incompatível.`
- conversão: `Um parâmetro da requisição possui formato inválido.`

- [ ] **Step 2: Run and verify RED**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: FAIL porque o Spring ainda usa seus Problem Details padrão.

- [ ] **Step 3: Override the Spring MVC handlers**

Adicionar a `ProblemTypes`:

```java
public static final URI INVALID_REQUEST =
        URI.create("urn:medflow:problem:invalid-request");
```

Sobrescrever `handleHttpMessageNotReadable` e `handleTypeMismatch` com as
assinaturas do Spring Framework 7.0.9. Ambos criam o ProblemDetail pelo mesmo
helper, preservam os headers recebidos e retornam `HttpStatus.BAD_REQUEST`.

- [ ] **Step 4: Run and verify GREEN**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: todos os testes do handler passam.

- [ ] **Step 5: Commit invalid request handling**

```powershell
git add medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java
git commit -m "feat(error): padroniza requisicoes invalidas"
```

### Task 4: Falha inesperada sem vazamento

**Files:**
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/ProblemTypes.java`
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java`
- Modify: `medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: qualquer `Exception` não tratada por um handler mais específico.
- Produces: `500 ProblemDetail` seguro e log completo no servidor.

- [ ] **Step 1: Write the failing internal-error test**

Adicionar endpoint de teste que lança
`new IllegalStateException("segredo-interno-do-servidor")`. Verificar:

```java
.andExpect(status().isInternalServerError())
.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
.andExpect(jsonPath("$.type").value("urn:medflow:problem:internal-error"))
.andExpect(jsonPath("$.title").value("Erro interno"))
.andExpect(jsonPath("$.status").value(500))
.andExpect(jsonPath("$.detail").value(
        "Ocorreu um erro interno inesperado."))
.andExpect(content().string(not(containsString("segredo-interno-do-servidor"))));
```

- [ ] **Step 2: Run and verify RED**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: FAIL porque não existe resposta segura para exceções inesperadas.

- [ ] **Step 3: Implement the catch-all handler**

Adicionar a `ProblemTypes`:

```java
public static final URI INTERNAL_ERROR =
        URI.create("urn:medflow:problem:internal-error");
```

Adicionar `Logger` SLF4J e:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ProblemDetail> handleUnexpected(
        Exception exception,
        HttpServletRequest request) {
    LOGGER.error("Erro não tratado ao processar {}", request.getRequestURI(), exception);
    return createResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ProblemTypes.INTERNAL_ERROR,
            "Erro interno",
            "Ocorreu um erro interno inesperado.",
            URI.create(request.getRequestURI())
    );
}
```

- [ ] **Step 4: Run and verify GREEN**

Run:

```powershell
.\mvnw.cmd '-Dtest=GlobalExceptionHandlerTest' test
```

Expected: todos os cenários do handler passam e a mensagem secreta não aparece
no corpo HTTP.

- [ ] **Step 5: Commit safe internal errors**

```powershell
git add medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java
git commit -m "feat(error): protege resposta de falhas inesperadas"
```

### Task 5: Verificação integral e explicação

**Files:**
- Review: todos os arquivos listados neste plano.
- Review: `docs/superpowers/specs/2026-09-12-global-exception-handler-rfc9457-design.md`.

**Interfaces:**
- Consumes: implementação completa dos Tasks 1 a 4.
- Produces: suíte verde, diff revisado e explicação didática ao usuário.

- [ ] **Step 1: Run the complete test suite**

```powershell
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`, nenhum teste com falha ou erro.

- [ ] **Step 2: Check the working tree**

```powershell
git diff --check
git status --short
```

Expected: nenhum erro de whitespace; somente alterações conhecidas.

- [ ] **Step 3: Review the contract manually**

Confirmar, em cada teste HTTP, igualdade entre status do protocolo e `status`
do corpo, Content-Type `application/problem+json`, URN correta, title estável,
instance correto e ausência de informação interna.

- [ ] **Step 4: Explain the implementation to the user**

Explicar em português brasileiro:

1. o fluxo `Controller -> exceção -> handler -> ProblemDetail`;
2. o papel de `@RestControllerAdvice` e `@ExceptionHandler`;
3. por que estender `ResponseEntityExceptionHandler`;
4. a diferença entre `400`, `404`, `422` e `500`;
5. o significado de cada campo RFC 9457 e da extensão `errors`;
6. como adicionar uma nova exceção de domínio no futuro;
7. como executar e ler os testes sem mocks desta entrega.
