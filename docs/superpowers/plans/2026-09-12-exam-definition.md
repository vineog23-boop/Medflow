# ExamDefinition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar o catálogo de definições de exame no monólito Medflow, com criação, consulta, atualização e desativação sem exclusão física.

**Architecture:** `ExamDefinition` pertence ao módulo `exam` e segue a organização atual `domain -> persistence -> application -> web`. A comunicação permanece local; esta entrega não usa OpenFeign, RabbitMQ, API Gateway ou infraestrutura nova.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Data JPA, Bean Validation, Spring MVC, JUnit 5, Mockito e MockMvc.

**Spec:** `docs/superpowers/specs/2026-09-12-exam-definition-design.md`

## Global Constraints

- Não alterar dependências, migrations ou configuração de infraestrutura.
- Não expor `ExamDefinition` diretamente na API; usar DTOs.
- Usar injeção por construtor.
- Normalizar `code` com `trim()` e `toUpperCase(Locale.ROOT)`.
- Não permitir alteração de `code` nem exclusão física.
- Usar `ProblemDetail` RFC 9457 para `404` e `409`.
- Preservar `output/`, `tmp/` e alterações não relacionadas.
- Executar comandos Maven dentro de `medflow/`.

## Execution Override

- O usuário escolheu escrever manualmente na ordem `Entity -> Repository -> Service -> Controller -> testes`.
- As etapas de produção serão compiladas antes dos testes, em vez de usar TDD.
- A feature não estará pronta até os testes de domínio, aplicação, web e erros passarem.
- Vinícius será o driver; o agente atuará como navigator e liberará um micro-passo por vez.

---

## File Structure

- `SampleType.java`: tipos de amostra aceitos pelo catálogo.
- `ExamDefinition.java`: estado e invariantes da definição de exame.
- `ExamDefinitionRepository.java`: persistência e consulta de duplicidade.
- DTOs: contratos de criação, atualização e resposta.
- Exceções: ausência da definição e conflito de código.
- `ExamDefinitionService.java`: casos de uso do catálogo.
- `ProblemTypes.java` e `GlobalExceptionHandler.java`: contrato RFC 9457.
- `ExamDefinitionController.java`: endpoints `/api/v1/exam-definitions`.
- Testes equivalentes em `src/test/java`.

### Task 1: Entidade e tipo de amostra

**Files:**
- Create: `medflow/src/main/java/br/com/medflow/exam/domain/enums/SampleType.java`
- Create: `medflow/src/main/java/br/com/medflow/exam/domain/ExamDefinition.java`

**Interfaces:**
- Consumes: tipos Jakarta Persistence e Bean Validation já presentes no projeto.
- Produces: `ExamDefinition(String code, String name, SampleType sampleType)`, `update(String, SampleType)`, `deactivate()` e getters.

- [ ] **Step 1: Criar o enum de amostra**

```java
package br.com.medflow.exam.domain.enums;

public enum SampleType {
    BLOOD,
    URINE,
    STOOL,
    SWAB,
    OTHER
}
```

- [ ] **Step 2: Criar a estrutura JPA da entidade**

Criar `ExamDefinition` com `@Entity`, `@Table(name = "exam_definitions")`,
construtor protegido para JPA e estes campos:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@NotBlank
@Size(max = 50)
@Column(name = "code", nullable = false, unique = true, updatable = false, length = 50)
private String code;

@NotBlank
@Size(max = 150)
@Column(name = "name", nullable = false, length = 150)
private String name;

@NotNull
@Enumerated(EnumType.STRING)
@Column(name = "sample_type", nullable = false, length = 30)
private SampleType sampleType;

@Column(name = "active", nullable = false)
private boolean active;

@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt;

@Column(name = "updated_at", nullable = false)
private Instant updatedAt;

@Version
@Column(name = "version", nullable = false)
private Long version;
```

- [ ] **Step 3: Implementar criação e normalização**

```java
public ExamDefinition(String code, String name, SampleType sampleType) {
    String normalizedCode = normalizeCode(code);
    String normalizedName = normalizeName(name);
    validate(normalizedCode, normalizedName, sampleType);

    this.code = normalizedCode;
    this.name = normalizedName;
    this.sampleType = sampleType;
    this.active = true;

    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
}

private static String normalizeCode(String code) {
    return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
}

private static String normalizeName(String name) {
    return name == null ? null : name.trim();
}
```

O método `validate` deve lançar `IllegalArgumentException` com mensagens
específicas para código vazio, código maior que 50, nome vazio, nome maior que
150 e tipo de amostra nulo.

```java
private static void validate(
        String code,
        String name,
        SampleType sampleType) {
    if (code == null || code.isBlank()) {
        throw new IllegalArgumentException("Código do exame deve ser informado");
    }
    if (code.length() > 50) {
        throw new IllegalArgumentException(
                "Código do exame deve ter no máximo 50 caracteres");
    }
    validateNameAndSampleType(name, sampleType);
}

private static void validateNameAndSampleType(
        String name,
        SampleType sampleType) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Nome do exame deve ser informado");
    }
    if (name.length() > 150) {
        throw new IllegalArgumentException(
                "Nome do exame deve ter no máximo 150 caracteres");
    }
    if (sampleType == null) {
        throw new IllegalArgumentException("Tipo de amostra deve ser informado");
    }
}
```

- [ ] **Step 4: Implementar atualização e desativação**

```java
public void update(String name, SampleType sampleType) {
    String normalizedName = normalizeName(name);
    validateNameAndSampleType(normalizedName, sampleType);
    this.name = normalizedName;
    this.sampleType = sampleType;
    this.updatedAt = Instant.now();
}

public void deactivate() {
    if (!active) {
        return;
    }
    this.active = false;
    this.updatedAt = Instant.now();
}
```

Adicionar os getters; não criar setters:

```java
public UUID getId() {
    return id;
}

public String getCode() {
    return code;
}

public String getName() {
    return name;
}

public SampleType getSampleType() {
    return sampleType;
}

public boolean isActive() {
    return active;
}

public Instant getCreatedAt() {
    return createdAt;
}

public Instant getUpdatedAt() {
    return updatedAt;
}

public Long getVersion() {
    return version;
}
```

- [ ] **Step 5: Compilar**

```powershell
cd medflow
.\mvnw.cmd -DskipTests compile
```

Expected: `BUILD SUCCESS`.

### Task 2: Repository

**Files:**
- Create: `medflow/src/main/java/br/com/medflow/exam/persistence/ExamDefinitionRepository.java`

**Interfaces:**
- Consumes: `ExamDefinition` e `UUID`.
- Produces: CRUD JPA e `boolean existsByCode(String code)`.

- [ ] **Step 1: Criar o Repository**

```java
package br.com.medflow.exam.persistence;

import br.com.medflow.exam.domain.ExamDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamDefinitionRepository
        extends JpaRepository<ExamDefinition, UUID> {

    boolean existsByCode(String code);
}
```

- [ ] **Step 2: Compilar**

```powershell
cd medflow
.\mvnw.cmd -DskipTests compile
```

Expected: `BUILD SUCCESS`. Esta compilação valida a assinatura; o comportamento
da query derivada será validado com PostgreSQL na etapa de persistência.

### Task 3: DTOs, exceções e Service

**Files:**
- Create: `medflow/src/main/java/br/com/medflow/exam/application/dto/CreateExamDefinitionRequest.java`
- Create: `medflow/src/main/java/br/com/medflow/exam/application/dto/UpdateExamDefinitionRequest.java`
- Create: `medflow/src/main/java/br/com/medflow/exam/application/dto/ExamDefinitionResponse.java`
- Create: `medflow/src/main/java/br/com/medflow/exam/application/exception/ExamDefinitionNotFoundException.java`
- Create: `medflow/src/main/java/br/com/medflow/exam/application/exception/ExamDefinitionCodeConflictException.java`
- Create: `medflow/src/main/java/br/com/medflow/exam/application/ExamDefinitionService.java`

**Interfaces:**
- Consumes: `ExamDefinitionRepository` e DTOs de entrada.
- Produces: `create`, `findById`, `findAll`, `update` e `deactivate`.

- [ ] **Step 1: Criar os DTOs**

```java
public record CreateExamDefinitionRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull SampleType sampleType
) {
}

public record UpdateExamDefinitionRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull SampleType sampleType
) {
}

public record ExamDefinitionResponse(
        UUID id,
        String code,
        String name,
        SampleType sampleType,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public ExamDefinitionResponse(ExamDefinition definition) {
        this(
                definition.getId(),
                definition.getCode(),
                definition.getName(),
                definition.getSampleType(),
                definition.isActive(),
                definition.getCreatedAt(),
                definition.getUpdatedAt()
        );
    }
}
```

- [ ] **Step 2: Criar as exceções**

```java
public class ExamDefinitionNotFoundException extends RuntimeException {
    public ExamDefinitionNotFoundException(UUID id) {
        super("Definição de exame não encontrada: " + id);
    }
}

public class ExamDefinitionCodeConflictException extends RuntimeException {
    public ExamDefinitionCodeConflictException(String code) {
        super("Já existe uma definição de exame com o código: " + code);
    }
}
```

- [ ] **Step 3: Criar a Service com injeção por construtor**

Assinaturas públicas:

```java
public ExamDefinitionResponse create(CreateExamDefinitionRequest request)
public ExamDefinitionResponse findById(UUID id)
public Page<ExamDefinitionResponse> findAll(Pageable pageable)
public ExamDefinitionResponse update(UUID id, UpdateExamDefinitionRequest request)
public void deactivate(UUID id)
```

Fluxo de criação:

```java
ExamDefinition definition = new ExamDefinition(
        request.code(), request.name(), request.sampleType());

if (repository.existsByCode(definition.getCode())) {
    throw new ExamDefinitionCodeConflictException(definition.getCode());
}

return new ExamDefinitionResponse(repository.save(definition));
```

Busca comum para `findById`, `update` e `deactivate`:

```java
private ExamDefinition findEntity(UUID id) {
    return repository.findById(id)
            .orElseThrow(() -> new ExamDefinitionNotFoundException(id));
}
```

Implementar os métodos com estes corpos:

```java
public ExamDefinitionResponse findById(UUID id) {
    return new ExamDefinitionResponse(findEntity(id));
}

public Page<ExamDefinitionResponse> findAll(Pageable pageable) {
    return repository.findAll(pageable)
            .map(ExamDefinitionResponse::new);
}

public ExamDefinitionResponse update(
        UUID id,
        UpdateExamDefinitionRequest request) {
    ExamDefinition definition = findEntity(id);
    definition.update(request.name(), request.sampleType());
    return new ExamDefinitionResponse(repository.save(definition));
}

public void deactivate(UUID id) {
    ExamDefinition definition = findEntity(id);
    definition.deactivate();
    repository.save(definition);
}
```

- [ ] **Step 4: Compilar**

```powershell
cd medflow
.\mvnw.cmd -DskipTests compile
```

Expected: `BUILD SUCCESS`.

### Task 4: Erros RFC 9457

**Files:**
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/ProblemTypes.java`
- Modify: `medflow/src/main/java/br/com/medflow/shared/web/error/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: as duas exceções de `ExamDefinition`.
- Produces: respostas `404` e `409` em `application/problem+json`.

- [ ] **Step 1: Adicionar os tipos de problema**

```java
public static final URI EXAM_DEFINITION_NOT_FOUND =
        URI.create("urn:medflow:problem:exam-definition-not-found");
public static final URI EXAM_DEFINITION_CODE_CONFLICT =
        URI.create("urn:medflow:problem:exam-definition-code-conflict");
```

- [ ] **Step 2: Adicionar os handlers**

Criar handlers equivalentes aos existentes, usando `createResponse`:

```java
@ExceptionHandler(ExamDefinitionNotFoundException.class)
public ResponseEntity<ProblemDetail> handleExamDefinitionNotFound(
        ExamDefinitionNotFoundException exception,
        HttpServletRequest request)

@ExceptionHandler(ExamDefinitionCodeConflictException.class)
public ResponseEntity<ProblemDetail> handleExamDefinitionCodeConflict(
        ExamDefinitionCodeConflictException exception,
        HttpServletRequest request)
```

O primeiro usa `HttpStatus.NOT_FOUND`, título
`Definição de exame não encontrada` e a URN de ausência. O segundo usa
`HttpStatus.CONFLICT`, título `Código de exame já cadastrado` e a URN de
conflito.

- [ ] **Step 3: Compilar**

```powershell
cd medflow
.\mvnw.cmd -DskipTests compile
```

Expected: `BUILD SUCCESS`.

### Task 5: Controller

**Files:**
- Create: `medflow/src/main/java/br/com/medflow/exam/web/ExamDefinitionController.java`

**Interfaces:**
- Consumes: `ExamDefinitionService` e DTOs.
- Produces: cinco endpoints sob `/api/v1/exam-definitions`.

- [ ] **Step 1: Criar o Controller e injetar a Service**

```java
@RestController
@RequestMapping("/api/v1/exam-definitions")
public class ExamDefinitionController {

    private final ExamDefinitionService service;

    public ExamDefinitionController(ExamDefinitionService service) {
        this.service = service;
    }
}
```

- [ ] **Step 2: Implementar os endpoints**

```java
@PostMapping
public ResponseEntity<ExamDefinitionResponse> create(
        @Valid @RequestBody CreateExamDefinitionRequest request) {
    ExamDefinitionResponse response = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

@GetMapping
public ResponseEntity<Page<ExamDefinitionResponse>> findAll(
        @PageableDefault(size = 10, sort = "name") Pageable pageable) {
    return ResponseEntity.ok(service.findAll(pageable));
}

@GetMapping("/{id}")
public ResponseEntity<ExamDefinitionResponse> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(service.findById(id));
}

@PutMapping("/{id}")
public ResponseEntity<ExamDefinitionResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateExamDefinitionRequest request) {
    return ResponseEntity.ok(service.update(id, request));
}

@PatchMapping("/{id}/deactivation")
public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
    service.deactivate(id);
    return ResponseEntity.noContent().build();
}
```

Retornos: `201` na criação, `200` nas consultas e atualização, e `204` na
desativação.

- [ ] **Step 3: Compilar**

```powershell
cd medflow
.\mvnw.cmd -DskipTests compile
```

Expected: `BUILD SUCCESS`.

### Task 6: Testes da entidade

**Files:**
- Create: `medflow/src/test/java/br/com/medflow/exam/domain/ExamDefinitionTest.java`

**Interfaces:**
- Consumes: API pública de `ExamDefinition`.
- Produces: evidência das invariantes do domínio.

- [ ] **Step 1: Cobrir criação válida e normalização**

```java
@Test
void deveCriarDefinicaoAtivaENormalizarDados() {
    ExamDefinition definition = new ExamDefinition(
            "  hemograma  ", "  Hemograma completo  ", SampleType.BLOOD);

    assertThat(definition.getCode()).isEqualTo("HEMOGRAMA");
    assertThat(definition.getName()).isEqualTo("Hemograma completo");
    assertThat(definition.getSampleType()).isEqualTo(SampleType.BLOOD);
    assertThat(definition.isActive()).isTrue();
    assertThat(definition.getCreatedAt()).isNotNull();
    assertThat(definition.getUpdatedAt()).isNotNull();
}
```

- [ ] **Step 2: Cobrir entradas inválidas**

```java
@Test
void naoDeveCriarSemCodigo() {
    assertThatThrownBy(() -> new ExamDefinition(
            " ", "Hemograma", SampleType.BLOOD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Código do exame deve ser informado");
}

@Test
void naoDeveCriarComCodigoMaiorQueCinquentaCaracteres() {
    assertThatThrownBy(() -> new ExamDefinition(
            "A".repeat(51), "Hemograma", SampleType.BLOOD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Código do exame deve ter no máximo 50 caracteres");
}

@Test
void naoDeveCriarSemNome() {
    assertThatThrownBy(() -> new ExamDefinition(
            "HEMOGRAMA", null, SampleType.BLOOD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome do exame deve ser informado");
}

@Test
void naoDeveCriarComNomeMaiorQueCentoECinquentaCaracteres() {
    assertThatThrownBy(() -> new ExamDefinition(
            "HEMOGRAMA", "A".repeat(151), SampleType.BLOOD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome do exame deve ter no máximo 150 caracteres");
}

@Test
void naoDeveCriarSemTipoDeAmostra() {
    assertThatThrownBy(() -> new ExamDefinition(
            "HEMOGRAMA", "Hemograma", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tipo de amostra deve ser informado");
}
```

```java
@Test
void naoDeveCriarComCodigoNulo() {
    assertThatThrownBy(() -> new ExamDefinition(
            null, "Hemograma", SampleType.BLOOD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Código do exame deve ser informado");
}
```

- [ ] **Step 3: Cobrir atualização e desativação**

```java
@Test
void deveAtualizarNomeETipoSemAlterarCodigo() {
    ExamDefinition definition = new ExamDefinition(
            "HEMOGRAMA", "Hemograma", SampleType.BLOOD);

    definition.update("Hemograma completo", SampleType.OTHER);

    assertThat(definition.getCode()).isEqualTo("HEMOGRAMA");
    assertThat(definition.getName()).isEqualTo("Hemograma completo");
    assertThat(definition.getSampleType()).isEqualTo(SampleType.OTHER);
}

@Test
void deveDesativarDeFormaIdempotente() {
    ExamDefinition definition = new ExamDefinition(
            "HEMOGRAMA", "Hemograma", SampleType.BLOOD);

    definition.deactivate();
    Instant firstDeactivation = definition.getUpdatedAt();
    definition.deactivate();

    assertThat(definition.isActive()).isFalse();
    assertThat(definition.getUpdatedAt()).isEqualTo(firstDeactivation);
}

@Test
void naoDeveAtualizarComNomeVazio() {
    ExamDefinition definition = new ExamDefinition(
            "HEMOGRAMA", "Hemograma", SampleType.BLOOD);

    assertThatThrownBy(() -> definition.update(" ", SampleType.BLOOD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome do exame deve ser informado");
}

@Test
void naoDeveAtualizarSemTipoDeAmostra() {
    ExamDefinition definition = new ExamDefinition(
            "HEMOGRAMA", "Hemograma", SampleType.BLOOD);

    assertThatThrownBy(() -> definition.update("Hemograma", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tipo de amostra deve ser informado");
}
```

- [ ] **Step 4: Executar o teste de domínio**

```powershell
cd medflow
.\mvnw.cmd '-Dtest=ExamDefinitionTest' test
```

Expected: todos os testes de `ExamDefinitionTest` passam.

### Task 7: Testes da Service

**Files:**
- Create: `medflow/src/test/java/br/com/medflow/exam/application/ExamDefinitionServiceTest.java`

**Interfaces:**
- Consumes: `ExamDefinitionService` e Repository simulado.
- Produces: evidência da orquestração da aplicação.

- [ ] **Step 1: Preparar Service e Repository**

```java
private final ExamDefinitionRepository repository =
        mock(ExamDefinitionRepository.class);
private final ExamDefinitionService service =
        new ExamDefinitionService(repository);
```

- [ ] **Step 2: Cobrir criação e conflito**

```java
@Test
void deveCriarDefinicaoQuandoCodigoNaoExiste() {
    CreateExamDefinitionRequest request = new CreateExamDefinitionRequest(
            " hemograma ", "Hemograma", SampleType.BLOOD);
    when(repository.existsByCode("HEMOGRAMA")).thenReturn(false);
    when(repository.save(any(ExamDefinition.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    ExamDefinitionResponse response = service.create(request);

    assertThat(response.code()).isEqualTo("HEMOGRAMA");
    assertThat(response.active()).isTrue();
    verify(repository).save(any(ExamDefinition.class));
}

@Test
void naoDeveCriarDefinicaoComCodigoDuplicado() {
    CreateExamDefinitionRequest request = new CreateExamDefinitionRequest(
            "hemograma", "Hemograma", SampleType.BLOOD);
    when(repository.existsByCode("HEMOGRAMA")).thenReturn(true);

    assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(ExamDefinitionCodeConflictException.class)
            .hasMessage("Já existe uma definição de exame com o código: HEMOGRAMA");
    verify(repository, never()).save(any());
}
```

- [ ] **Step 3: Cobrir consulta e paginação**

```java
@Test
void deveBuscarDefinicaoPorId() {
    UUID id = UUID.randomUUID();
    ExamDefinition definition = definition();
    when(repository.findById(id)).thenReturn(Optional.of(definition));

    ExamDefinitionResponse response = service.findById(id);

    assertThat(response.code()).isEqualTo("HEMOGRAMA");
}

@Test
void deveLancarExcecaoQuandoDefinicaoNaoExistir() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(ExamDefinitionNotFoundException.class);
}

@Test
void deveListarDefinicoesPaginadas() {
    PageRequest pageable = PageRequest.of(0, 10);
    when(repository.findAll(pageable)).thenReturn(
            new PageImpl<>(List.of(definition()), pageable, 1));

    Page<ExamDefinitionResponse> result = service.findAll(pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getNumber()).isZero();
    assertThat(result.getSize()).isEqualTo(10);
}
```

- [ ] **Step 4: Cobrir atualização e desativação**

```java
@Test
void deveAtualizarDefinicao() {
    UUID id = UUID.randomUUID();
    ExamDefinition definition = definition();
    when(repository.findById(id)).thenReturn(Optional.of(definition));
    when(repository.save(definition)).thenReturn(definition);

    ExamDefinitionResponse response = service.update(id,
            new UpdateExamDefinitionRequest("Hemograma completo", SampleType.OTHER));

    assertThat(response.name()).isEqualTo("Hemograma completo");
    assertThat(response.sampleType()).isEqualTo(SampleType.OTHER);
}

@Test
void deveDesativarDefinicao() {
    UUID id = UUID.randomUUID();
    ExamDefinition definition = definition();
    when(repository.findById(id)).thenReturn(Optional.of(definition));

    service.deactivate(id);

    assertThat(definition.isActive()).isFalse();
    verify(repository).save(definition);
}
```

```java
@Test
void naoDeveAtualizarDefinicaoInexistente() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(id,
            new UpdateExamDefinitionRequest("Hemograma", SampleType.BLOOD)))
            .isInstanceOf(ExamDefinitionNotFoundException.class);
    verify(repository, never()).save(any());
}

@Test
void naoDeveDesativarDefinicaoInexistente() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deactivate(id))
            .isInstanceOf(ExamDefinitionNotFoundException.class);
    verify(repository, never()).save(any());
}
```

Usar este helper:

```java
private ExamDefinition definition() {
    return new ExamDefinition("HEMOGRAMA", "Hemograma", SampleType.BLOOD);
}
```

- [ ] **Step 5: Executar o teste da Service**

```powershell
cd medflow
.\mvnw.cmd '-Dtest=ExamDefinitionServiceTest' test
```

Expected: todos os testes de `ExamDefinitionServiceTest` passam.

### Task 8: Testes web, erros, documentação e gate final

**Files:**
- Create: `medflow/src/test/java/br/com/medflow/exam/web/ExamDefinitionControllerTest.java`
- Modify: `medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java`
- Modify: `README.md`

**Interfaces:**
- Consumes: Controller, handler e contratos implementados.
- Produces: contrato HTTP verificado e documentação factual.

- [ ] **Step 1: Criar testes do Controller**

Usar `PageableHandlerMethodArgumentResolver`, Validator real,
`GlobalExceptionHandler` real e `ExamDefinitionService` simulada. Preparar:

```java
private static final UUID ID = UUID.fromString(
        "76cb605b-71dc-47e8-83b5-83f2ef99a7cf");

private final ExamDefinitionService service = mock(ExamDefinitionService.class);
private final MockMvc mockMvc;

ExamDefinitionControllerTest() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    this.mockMvc = standaloneSetup(new ExamDefinitionController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
}

private ExamDefinitionResponse response() {
    return new ExamDefinitionResponse(
            ID,
            "HEMOGRAMA",
            "Hemograma",
            SampleType.BLOOD,
            true,
            Instant.parse("2026-09-12T12:00:00Z"),
            Instant.parse("2026-09-12T12:00:00Z")
    );
}
```

Cobrir exatamente:

```text
POST  /api/v1/exam-definitions                  -> 201
GET   /api/v1/exam-definitions                  -> 200 com Page
GET   /api/v1/exam-definitions/{id}             -> 200
PUT   /api/v1/exam-definitions/{id}             -> 200
PATCH /api/v1/exam-definitions/{id}/deactivation -> 204
```

```java
@Test
void deveCriarDefinicao() throws Exception {
    when(service.create(any())).thenReturn(response());

    mockMvc.perform(post("/api/v1/exam-definitions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "code": "HEMOGRAMA",
                              "name": "Hemograma",
                              "sampleType": "BLOOD"
                            }
                            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.code").value("HEMOGRAMA"));

    verify(service).create(new CreateExamDefinitionRequest(
            "HEMOGRAMA", "Hemograma", SampleType.BLOOD));
}

@Test
void deveListarDefinicoes() throws Exception {
    when(service.findAll(any())).thenReturn(new PageImpl<>(List.of(response())));

    mockMvc.perform(get("/api/v1/exam-definitions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].code").value("HEMOGRAMA"))
            .andExpect(jsonPath("$.content[0].sampleType").value("BLOOD"))
            .andExpect(jsonPath("$.content[0].active").value(true));
}

@Test
void deveBuscarDefinicaoPorId() throws Exception {
    when(service.findById(ID)).thenReturn(response());

    mockMvc.perform(get("/api/v1/exam-definitions/{id}", ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.name").value("Hemograma"));
}

@Test
void deveAtualizarDefinicao() throws Exception {
    when(service.update(eq(ID), any())).thenReturn(response());

    mockMvc.perform(put("/api/v1/exam-definitions/{id}", ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "Hemograma",
                              "sampleType": "BLOOD"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("HEMOGRAMA"));

    verify(service).update(ID, new UpdateExamDefinitionRequest(
            "Hemograma", SampleType.BLOOD));
}

@Test
void deveDesativarDefinicao() throws Exception {
    mockMvc.perform(patch(
                    "/api/v1/exam-definitions/{id}/deactivation", ID))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

    verify(service).deactivate(ID);
}

@Test
void naoDeveCriarDefinicaoComDadosInvalidos() throws Exception {
    mockMvc.perform(post("/api/v1/exam-definitions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "code": "",
                              "name": "",
                              "sampleType": null
                            }
                            """))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.type").value(
                    "urn:medflow:problem:validation-error"));

    verifyNoInteractions(service);
}
```

- [ ] **Step 2: Cobrir os novos Problem Details**

Adicionar ao Controller de prova de `GlobalExceptionHandlerTest`:

```java
@GetMapping("/exam-definitions/{id}")
void examDefinitionNotFound(@PathVariable UUID id) {
    throw new ExamDefinitionNotFoundException(id);
}

@PostMapping("/exam-definitions/conflict/{code}")
void examDefinitionConflict(@PathVariable String code) {
    throw new ExamDefinitionCodeConflictException(code);
}
```

Criar dois testes com `MockMvc` e verificar:

```text
404 + urn:medflow:problem:exam-definition-not-found
409 + urn:medflow:problem:exam-definition-code-conflict
```

Requisições e asserções completas:

```java
mockMvc.perform(get("/test-errors/exam-definitions/{id}", ID))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value(
                "urn:medflow:problem:exam-definition-not-found"))
        .andExpect(jsonPath("$.title").value(
                "Definição de exame não encontrada"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value(
                "Definição de exame não encontrada: " + ID))
        .andExpect(jsonPath("$.instance").value(
                "/test-errors/exam-definitions/" + ID));

mockMvc.perform(post(
                "/test-errors/exam-definitions/conflict/{code}", "HEMOGRAMA"))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value(
                "urn:medflow:problem:exam-definition-code-conflict"))
        .andExpect(jsonPath("$.title").value("Código de exame já cadastrado"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value(
                "Já existe uma definição de exame com o código: HEMOGRAMA"))
        .andExpect(jsonPath("$.instance").value(
                "/test-errors/exam-definitions/conflict/HEMOGRAMA"));
```

- [ ] **Step 3: Atualizar o README**

Documentar somente o catálogo realmente implementado, suas cinco rotas, a
desativação sem exclusão e os erros `404`/`409`. Não afirmar que PostgreSQL,
Security, RabbitMQ ou Gateway estão funcionais.

- [ ] **Step 4: Executar a suíte completa**

```powershell
cd medflow
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`, zero falhas, zero erros e zero testes ignorados.

- [ ] **Step 5: Revisar o diff**

```powershell
cd ..
git diff --check
git status --short
```

Expected: nenhum erro de whitespace; `output/` e `tmp/` permanecem sem stage.

- [ ] **Step 6: Criar o commit da feature**

```powershell
git add -- medflow/src/main/java/br/com/medflow/exam medflow/src/main/java/br/com/medflow/shared/web/error medflow/src/test/java/br/com/medflow/exam medflow/src/test/java/br/com/medflow/shared/web/error/GlobalExceptionHandlerTest.java README.md docs/superpowers/plans/2026-09-12-exam-definition.md
git commit -m "feat(exam): adiciona catálogo de exames"
```
