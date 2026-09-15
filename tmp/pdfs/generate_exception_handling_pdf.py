from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.colors import HexColor
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    Flowable,
    KeepTogether,
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "output" / "pdf" / "tratamento-global-excecoes-medflow.pdf"

NAVY = HexColor("#0B1F33")
TEAL = HexColor("#0B7A75")
TEAL_DARK = HexColor("#075E59")
BLUE = HexColor("#2563EB")
ORANGE = HexColor("#D97706")
RED = HexColor("#B42318")
GREEN = HexColor("#15803D")
INK = HexColor("#1E293B")
MUTED = HexColor("#526175")
LIGHT = HexColor("#F3F6F9")
LINE = HexColor("#D6DEE8")
WHITE = colors.white


class SectionRule(Flowable):
    def __init__(self, width=18 * mm, color=TEAL):
        super().__init__()
        self.width = width
        self.height = 3
        self.color = color

    def draw(self):
        self.canv.setStrokeColor(self.color)
        self.canv.setLineWidth(3)
        self.canv.line(0, 0, self.width, 0)


styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="CoverTitle",
    parent=styles["Title"],
    fontName="Helvetica-Bold",
    fontSize=29,
    leading=34,
    textColor=WHITE,
    alignment=TA_LEFT,
    spaceAfter=8 * mm,
))
styles.add(ParagraphStyle(
    name="CoverSubtitle",
    parent=styles["Normal"],
    fontName="Helvetica",
    fontSize=13,
    leading=19,
    textColor=HexColor("#D7E7EF"),
    spaceAfter=5 * mm,
))
styles.add(ParagraphStyle(
    name="CoverMetaLabel",
    parent=styles["Normal"],
    fontName="Helvetica-Bold",
    fontSize=7.5,
    leading=10,
    textColor=HexColor("#8DB4C7"),
))
styles.add(ParagraphStyle(
    name="CoverMetaValue",
    parent=styles["Normal"],
    fontName="Helvetica-Bold",
    fontSize=9.2,
    leading=12,
    textColor=WHITE,
))
styles.add(ParagraphStyle(
    name="Eyebrow",
    parent=styles["Normal"],
    fontName="Helvetica-Bold",
    fontSize=8.5,
    leading=11,
    textColor=TEAL,
    spaceAfter=2 * mm,
))
styles.add(ParagraphStyle(
    name="H1Custom",
    parent=styles["Heading1"],
    fontName="Helvetica-Bold",
    fontSize=20,
    leading=25,
    textColor=NAVY,
    spaceBefore=1 * mm,
    spaceAfter=3 * mm,
))
styles.add(ParagraphStyle(
    name="H2Custom",
    parent=styles["Heading2"],
    fontName="Helvetica-Bold",
    fontSize=13,
    leading=17,
    textColor=TEAL_DARK,
    spaceBefore=4 * mm,
    spaceAfter=2 * mm,
))
styles.add(ParagraphStyle(
    name="BodyCustom",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=9.6,
    leading=14.2,
    textColor=INK,
    spaceAfter=2.5 * mm,
))
styles.add(ParagraphStyle(
    name="Small",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=8.2,
    leading=11.5,
    textColor=MUTED,
))
styles.add(ParagraphStyle(
    name="CardTitle",
    parent=styles["BodyText"],
    fontName="Helvetica-Bold",
    fontSize=10.2,
    leading=13,
    textColor=NAVY,
    spaceAfter=1.2 * mm,
))
styles.add(ParagraphStyle(
    name="CardBody",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=8.6,
    leading=12,
    textColor=INK,
))
styles.add(ParagraphStyle(
    name="Callout",
    parent=styles["BodyText"],
    fontName="Helvetica-Bold",
    fontSize=10.3,
    leading=15,
    textColor=TEAL_DARK,
    alignment=TA_CENTER,
))
styles.add(ParagraphStyle(
    name="CodeBlock",
    fontName="Courier",
    fontSize=7.2,
    leading=9.4,
    textColor=HexColor("#DCE7F1"),
    leftIndent=0,
    rightIndent=0,
))
styles.add(ParagraphStyle(
    name="TableHeader",
    parent=styles["BodyText"],
    fontName="Helvetica-Bold",
    fontSize=8.4,
    leading=11,
    textColor=WHITE,
    alignment=TA_LEFT,
))
styles.add(ParagraphStyle(
    name="TableCell",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=8.2,
    leading=11.2,
    textColor=INK,
))
styles.add(ParagraphStyle(
    name="BulletCustom",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=9.2,
    leading=13.3,
    textColor=INK,
    leftIndent=5 * mm,
    firstLineIndent=-3 * mm,
    bulletIndent=1 * mm,
    spaceAfter=1.5 * mm,
))


def p(text, style="BodyCustom"):
    return Paragraph(text, styles[style])


def bullet(text):
    return Paragraph(text, styles["BulletCustom"], bulletText="•")


def heading(number, title, subtitle=None):
    parts = [p(f"SEÇÃO {number}", "Eyebrow"), p(title, "H1Custom"), SectionRule()]
    if subtitle:
        parts.extend([Spacer(1, 3 * mm), p(subtitle, "BodyCustom")])
    else:
        parts.append(Spacer(1, 3 * mm))
    return parts


def card(title, body, accent=TEAL):
    content = [p(title, "CardTitle"), p(body, "CardBody")]
    table = Table([[content]], colWidths=[76 * mm])
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), LIGHT),
        ("BOX", (0, 0), (-1, -1), 0.7, LINE),
        ("LINEBEFORE", (0, 0), (0, -1), 3, accent),
        ("LEFTPADDING", (0, 0), (-1, -1), 5 * mm),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4 * mm),
        ("TOPPADDING", (0, 0), (-1, -1), 4 * mm),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4 * mm),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ]))
    return table


def code_box(code):
    block = Preformatted(code.strip(), styles["CodeBlock"])
    table = Table([[block]], colWidths=[166 * mm])
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), NAVY),
        ("BOX", (0, 0), (-1, -1), 0.8, HexColor("#203A54")),
        ("LEFTPADDING", (0, 0), (-1, -1), 4 * mm),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4 * mm),
        ("TOPPADDING", (0, 0), (-1, -1), 3.5 * mm),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3.5 * mm),
    ]))
    return table


def data_table(rows, widths):
    prepared = []
    for row_index, row in enumerate(rows):
        style = "TableHeader" if row_index == 0 else "TableCell"
        prepared.append([p(cell, style) for cell in row])
    table = Table(prepared, colWidths=widths, repeatRows=1)
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [WHITE, LIGHT]),
        ("GRID", (0, 0), (-1, -1), 0.45, LINE),
        ("LEFTPADDING", (0, 0), (-1, -1), 3 * mm),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3 * mm),
        ("TOPPADDING", (0, 0), (-1, -1), 2.6 * mm),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2.6 * mm),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ]))
    return table


def draw_cover(canvas, doc):
    width, height = A4
    canvas.saveState()
    canvas.setFillColor(NAVY)
    canvas.rect(0, 0, width, height, fill=1, stroke=0)
    canvas.setFillColor(TEAL)
    canvas.rect(0, 0, 13 * mm, height, fill=1, stroke=0)
    canvas.setFillColor(HexColor("#173B55"))
    canvas.circle(width - 23 * mm, height - 24 * mm, 38 * mm, fill=1, stroke=0)
    canvas.setFillColor(HexColor("#0D5C63"))
    canvas.circle(width - 4 * mm, 25 * mm, 43 * mm, fill=1, stroke=0)
    canvas.restoreState()


def draw_later_page(canvas, doc):
    width, height = A4
    canvas.saveState()
    canvas.setStrokeColor(LINE)
    canvas.setLineWidth(0.5)
    canvas.line(20 * mm, height - 15 * mm, width - 20 * mm, height - 15 * mm)
    canvas.setFont("Helvetica-Bold", 7.5)
    canvas.setFillColor(TEAL_DARK)
    canvas.drawString(20 * mm, height - 11.5 * mm, "MEDFLOW  |  GUIA DE ESTUDO")
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(MUTED)
    canvas.drawRightString(width - 20 * mm, height - 11.5 * mm, "Tratamento global de exceções")
    canvas.line(20 * mm, 14 * mm, width - 20 * mm, 14 * mm)
    canvas.drawString(20 * mm, 9.5 * mm, "Java 21  |  Spring Boot 4.1.1  |  RFC 9457")
    canvas.drawRightString(width - 20 * mm, 9.5 * mm, f"Página {doc.page}")
    canvas.restoreState()


story = []

# Capa
story.extend([
    Spacer(1, 34 * mm),
    p("GUIA DE ESTUDO - JAVA BACKEND", "Eyebrow"),
    p("Tratamento global<br/>de exceções", "CoverTitle"),
    p(
        "Como o Medflow transforma falhas de domínio, validação e infraestrutura "
        "em respostas HTTP consistentes e seguras.",
        "CoverSubtitle",
    ),
    Spacer(1, 12 * mm),
])
cover_meta = Table([
    [p("STACK", "CoverMetaLabel"), p("CONTRATO", "CoverMetaLabel"), p("ESCOPO", "CoverMetaLabel")],
    [p("Java 21<br/>Spring Boot 4.1.1", "CoverMetaValue"),
     p("Problem Details<br/>RFC 9457", "CoverMetaValue"),
     p("Patient + Exam<br/>Validação + Segurança", "CoverMetaValue")],
], colWidths=[52 * mm, 52 * mm, 52 * mm])
cover_meta.setStyle(TableStyle([
    ("BACKGROUND", (0, 0), (-1, -1), HexColor("#15364E")),
    ("TEXTCOLOR", (0, 0), (-1, -1), WHITE),
    ("BOX", (0, 0), (-1, -1), 0.7, HexColor("#31566F")),
    ("INNERGRID", (0, 0), (-1, -1), 0.5, HexColor("#31566F")),
    ("LEFTPADDING", (0, 0), (-1, -1), 4 * mm),
    ("RIGHTPADDING", (0, 0), (-1, -1), 4 * mm),
    ("TOPPADDING", (0, 0), (-1, -1), 3 * mm),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 3 * mm),
]))
story.extend([
    cover_meta,
    Spacer(1, 43 * mm),
    p("Projeto Medflow", "CoverSubtitle"),
    p("Material técnico para revisão de Vinícius Oliveira", "CoverSubtitle"),
    PageBreak(),
])

# Seção 1
story.extend(heading(
    "01",
    "O problema que estamos resolvendo",
    "Uma API precisa falhar de modo previsível. O cliente não deve conhecer as classes internas "
    "da aplicação nem receber um formato diferente em cada endpoint.",
))
story.append(Spacer(1, 2 * mm))
flow = Table([[
    p("Requisição", "Callout"), p("->", "Callout"),
    p("Controller", "Callout"), p("->", "Callout"),
    p("Exceção", "Callout"), p("->", "Callout"),
    p("Handler", "Callout"), p("->", "Callout"),
    p("ProblemDetail", "Callout"),
]], colWidths=[28*mm, 7*mm, 27*mm, 7*mm, 24*mm, 7*mm, 24*mm, 7*mm, 35*mm])
flow.setStyle(TableStyle([
    ("BACKGROUND", (0, 0), (-1, -1), LIGHT),
    ("BOX", (0, 0), (-1, -1), 0.7, LINE),
    ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ("TOPPADDING", (0, 0), (-1, -1), 4 * mm),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 4 * mm),
]))
story.extend([
    flow,
    Spacer(1, 7 * mm),
    p("Ideia central", "H2Custom"),
    p(
        "A camada de negócio continua responsável por detectar a falha e lançar uma exceção. "
        "A camada web decide como essa falha será apresentada por HTTP. Essa divisão evita "
        "acoplamento entre regra de negócio e protocolo.",
    ),
    Spacer(1, 3 * mm),
])
cards = Table([[
    card("Sem tratamento global", "Cada Controller repete try/catch, escolhe formatos diferentes e pode vazar detalhes internos.", RED),
    card("Com tratamento global", "Um único ponto traduz exceções para status, mídia e estrutura estáveis.", GREEN),
]], colWidths=[82 * mm, 82 * mm])
cards.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 3 * mm)]))
story.extend([
    cards,
    Spacer(1, 8 * mm),
    p("Regra mental", "H2Custom"),
    card(
        "Quem detecta não precisa formatar",
        "Service lança a exceção. GlobalExceptionHandler escolhe o status e monta a resposta. Controller mantém o fluxo normal.",
        BLUE,
    ),
    PageBreak(),
])

# Seção 2
story.extend(heading(
    "02",
    "Por que shared.web.error?",
    "O nome do pacote comunica alcance, camada e responsabilidade. Não é uma arquitetura nova: "
    "é apenas uma casa clara para uma preocupação transversal da borda HTTP.",
))
package_rows = [
    ["Parte", "Significado", "Decisão no Medflow"],
    ["shared", "Compartilhado entre módulos.", "Patient e Exam usam o mesmo contrato de erro."],
    ["web", "Pertence à interface HTTP.", "Não coloca ProblemDetail dentro do domínio."],
    ["error", "Agrupa a tradução de falhas.", "Evita espalhar handlers pelos Controllers."],
]
story.extend([
    data_table(package_rows, [25 * mm, 52 * mm, 89 * mm]),
    Spacer(1, 7 * mm),
    p("Os três arquivos", "H2Custom"),
])
files = Table([[
    card("GlobalExceptionHandler", "Orquestra a tradução: recebe exceção, escolhe status e cria ProblemDetail.", TEAL),
    card("ProblemTypes", "É o catálogo das URNs estáveis que identificam cada categoria de problema.", BLUE),
], [
    card("ValidationError", "É o contrato de um erro de campo: mensagem legível e localização no JSON.", ORANGE),
    card("Quando dividir mais?", "Somente quando o handler crescer por novos módulos. Hoje, três arquivos são suficientes.", GREEN),
]], colWidths=[82 * mm, 82 * mm])
files.setStyle(TableStyle([
    ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ("LEFTPADDING", (0, 0), (-1, -1), 0),
    ("RIGHTPADDING", (0, 0), (-1, -1), 3 * mm),
    ("TOPPADDING", (0, 0), (-1, -1), 1.5 * mm),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 1.5 * mm),
]))
story.extend([
    files,
    Spacer(1, 8 * mm),
    p("Leitura profissional", "H2Custom"),
    bullet("Separar arquivos não significa aplicar Clean Architecture completa."),
    bullet("Cada arquivo possui uma responsabilidade e um motivo concreto para existir."),
    bullet("Juntar tudo reduziria navegação, mas aumentaria mistura de conceitos dentro do handler."),
    bullet("O nível técnico está no entendimento das decisões, não na quantidade de classes."),
    PageBreak(),
])

# Seção 3
story.extend(heading(
    "03",
    "Problem Details e RFC 9457",
    "ProblemDetail oferece uma linguagem comum para erros HTTP. Clientes podem tratar a "
    "categoria do problema sem depender de mensagens humanas instáveis.",
))
fields = [
    ["Campo", "Função", "Exemplo"],
    ["type", "Identificador estável da categoria.", "urn:medflow:problem:patient-not-found"],
    ["title", "Resumo curto e estável.", "Paciente não encontrado"],
    ["status", "Código HTTP também presente no corpo.", "404"],
    ["detail", "Explica aquela ocorrência.", "Paciente com id ... não encontrado"],
    ["instance", "Identifica a requisição afetada.", "/patients/{id}"],
    ["errors", "Extensão para violações de campos.", "detail + pointer"],
]
story.extend([
    data_table(fields, [23 * mm, 61 * mm, 82 * mm]),
    Spacer(1, 6 * mm),
    p("Exemplo de resposta", "H2Custom"),
    code_box('''{
  "type": "urn:medflow:problem:patient-not-found",
  "title": "Paciente não encontrado",
  "status": 404,
  "detail": "Paciente com id: 76cb... não encontrado",
  "instance": "/patients/76cb..."
}'''),
    Spacer(1, 6 * mm),
    card(
        "Por que uma URN?",
        "O campo type precisa de um identificador URI estável. A URN cumpre esse papel sem exigir que o projeto possua um domínio público.",
        TEAL,
    ),
    PageBreak(),
])

# Seção 4
story.extend(heading(
    "04",
    "Escolha dos status HTTP",
    "O status deve descrever o tipo de falha observado pelo cliente. O corpo não substitui o "
    "status: ambos precisam contar a mesma história.",
))
statuses = [
    ["Status", "Quando usar", "Cenário no Medflow"],
    ["400", "A requisição não pôde ser interpretada corretamente.", "JSON quebrado ou UUID em formato inválido."],
    ["404", "O recurso solicitado não existe.", "Paciente ou ordem de exame não encontrados."],
    ["422", "A sintaxe foi entendida, mas os dados violam o contrato.", "CPF inválido, nome vazio ou data futura."],
    ["500", "O servidor encontrou uma falha inesperada.", "Exceção sem tratamento mais específico."],
]
story.extend([
    data_table(statuses, [20 * mm, 69 * mm, 77 * mm]),
    Spacer(1, 8 * mm),
    p("Como o Spring entra no fluxo", "H2Custom"),
    bullet("@RestControllerAdvice torna o handler visível para todos os Controllers."),
    bullet("@ExceptionHandler liga uma exceção de domínio a um método específico."),
    bullet("ResponseEntityExceptionHandler oferece pontos de extensão para falhas geradas pelo próprio Spring MVC."),
    bullet("Os overrides preservam os headers do Spring e substituem apenas o corpo e o status necessários."),
    Spacer(1, 5 * mm),
    card(
        "Por que não colocar try/catch no Controller?",
        "Porque o Controller deveria coordenar entrada e saída. Repetir tratamento em cada endpoint aumenta duplicação e facilita contratos inconsistentes.",
        RED,
    ),
    PageBreak(),
])

# Seção 5
story.extend(heading(
    "05",
    "Validação: do DTO ao campo inválido",
    "Quando @Valid encontra violações no PatientDtoRequest, o Spring lança "
    "MethodArgumentNotValidException antes de executar o método do Controller.",
))
story.extend([
    p("Transformação realizada pela Stream", "H2Custom"),
    code_box('''List<ValidationError> errors = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fieldError -> new ValidationError(
                fieldError.getDefaultMessage() != null
                        ? fieldError.getDefaultMessage()
                        : "Valor inválido",
                toJsonPointer(fieldError.getField())
        ))
        .sorted((first, second) ->
                first.pointer().compareTo(second.pointer()))
        .toList();'''),
    Spacer(1, 6 * mm),
])
steps = [
    ["Etapa", "Responsabilidade"],
    ["getFieldErrors", "Obtém somente as violações associadas a campos."],
    ["stream", "Inicia o processamento declarativo da coleção."],
    ["map", "Transforma FieldError do Spring em ValidationError da API."],
    ["sorted", "Garante ordem determinística pelo JSON Pointer."],
    ["toList", "Materializa o resultado que será colocado em errors."],
]
story.extend([
    data_table(steps, [40 * mm, 126 * mm]),
    Spacer(1, 6 * mm),
    p("JSON Pointer", "H2Custom"),
    p(
        "O pointer localiza o campo no documento recebido. Um caminho Java como "
        "<b>address.street</b> vira <b>#/address/street</b>. Os caracteres reservados "
        "<b>~</b> e <b>/</b> são escapados conforme a RFC 6901.",
    ),
    PageBreak(),
])

# Seção 6
story.extend(heading(
    "06",
    "Segurança e falhas inesperadas",
    "Uma exceção inesperada precisa gerar duas saídas diferentes: informação completa para "
    "quem opera o servidor e informação segura para o consumidor da API.",
))
security = Table([[
    card("Log do servidor", "Recebe mensagem, classe da exceção e stack trace. Serve para investigação técnica.", ORANGE),
    card("Resposta do cliente", "Recebe um ProblemDetail estável, sem SQL, stack trace, credenciais ou detalhes internos.", GREEN),
]], colWidths=[82 * mm, 82 * mm])
security.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 3 * mm)]))
story.extend([
    security,
    Spacer(1, 7 * mm),
    code_box('''@ExceptionHandler(Exception.class)
public ResponseEntity<ProblemDetail> handleUnexpected(
        Exception exception,
        HttpServletRequest request) {

    LOGGER.error("Erro não tratado ao processar {}",
            request.getRequestURI(), exception);

    return createResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ProblemTypes.INTERNAL_ERROR,
            "Erro interno",
            "Ocorreu um erro interno inesperado.",
            URI.create(request.getRequestURI())
    );
}'''),
    Spacer(1, 6 * mm),
    card(
        "Regra de segurança",
        "Nunca use exception.getMessage() no corpo de um erro 500 genérico. A mensagem pode conter detalhes de banco, caminhos, tokens ou dados sensíveis.",
        RED,
    ),
    PageBreak(),
])

# Seção 7
story.extend(heading(
    "07",
    "Como adicionar um novo tratamento",
    "O processo deve começar pela semântica da falha, não pelo status HTTP. Primeiro modele a "
    "exceção; depois defina como a borda web a representa.",
))
new_handler = '''@ExceptionHandler(DoctorNotFoundException.class)
public ResponseEntity<ProblemDetail> handleDoctorNotFound(
        DoctorNotFoundException exception,
        HttpServletRequest request) {
    return createResponse(
            HttpStatus.NOT_FOUND,
            ProblemTypes.DOCTOR_NOT_FOUND,
            "Médico não encontrado",
            exception.getMessage(),
            URI.create(request.getRequestURI())
    );
}'''
checklist = [
    ["Passo", "Pergunta de revisão"],
    ["1. Exceção", "Ela representa uma falha real do domínio ou da aplicação?"],
    ["2. Type", "A URN é única, estável e adicionada em ProblemTypes?"],
    ["3. Status", "O código HTTP descreve corretamente o que o cliente pode fazer?"],
    ["4. Detail", "A mensagem ajuda sem revelar informação interna?"],
    ["5. Teste", "Status, content-type, campos RFC e instance foram verificados?"],
]
story.extend([
    data_table(checklist, [31 * mm, 135 * mm]),
    Spacer(1, 6 * mm),
    p("Exemplo", "H2Custom"),
    code_box(new_handler),
    Spacer(1, 6 * mm),
    p("O que saber explicar em uma entrevista", "H2Custom"),
    bullet("A regra de negócio lança a exceção; a camada web traduz para HTTP."),
    bullet("ProblemDetail cria um contrato previsível e interoperável."),
    bullet("400, 404, 422 e 500 representam falhas diferentes."),
    bullet("O catch-all protege o cliente e mantém o diagnóstico no log."),
    PageBreak(),
])

# Seção 8
story.extend(heading(
    "08",
    "Testes e checklist final",
    "Os testes desta entrega exercitam o handler e o Validator reais por meio do MockMvc, sem "
    "Mockito e sem substituir Service ou Repository por mocks.",
))
tests = [
    ["Cenário", "Evidência protegida"],
    ["Paciente ausente", "404 + type patient-not-found + instance correto."],
    ["Ordem ausente", "404 + type exam-order-not-found."],
    ["DTO inválido", "422 + lista errors ordenada com JSON Pointer."],
    ["JSON quebrado", "400 + mensagem pública estável."],
    ["UUID inválido", "400 antes da execução do Controller."],
    ["Falha inesperada", "500 sem vazamento da mensagem interna."],
]
story.extend([
    data_table(tests, [55 * mm, 111 * mm]),
    Spacer(1, 7 * mm),
    p("Comando de verificação", "H2Custom"),
    code_box(r'''.\mvnw.cmd test

Tests run: 44, Failures: 0, Errors: 0
BUILD SUCCESS'''),
    Spacer(1, 7 * mm),
    card(
        "MockMvc não é Mockito",
        "MockMvc simula a infraestrutura HTTP sem abrir uma porta de servidor. Nesta entrega, GlobalExceptionHandler e Validator são objetos reais; não há mocks de interação entre classes.",
        BLUE,
    ),
    Spacer(1, 7 * mm),
    p("Referências", "H2Custom"),
    p("RFC 9457 - Problem Details for HTTP APIs<br/>https://www.rfc-editor.org/rfc/rfc9457.html", "Small"),
    p("RFC 9110 - HTTP Semantics<br/>https://www.rfc-editor.org/rfc/rfc9110.html", "Small"),
    p("RFC 6901 - JavaScript Object Notation Pointer<br/>https://www.rfc-editor.org/rfc/rfc6901.html", "Small"),
    p("Spring MVC - Error Responses<br/>https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html", "Small"),
])


OUTPUT.parent.mkdir(parents=True, exist_ok=True)
document = SimpleDocTemplate(
    str(OUTPUT),
    pagesize=A4,
    rightMargin=22 * mm,
    leftMargin=22 * mm,
    topMargin=22 * mm,
    bottomMargin=20 * mm,
    title="Tratamento global de exceções no Medflow",
    author="Vinícius Oliveira",
    subject="Spring Boot, ProblemDetail, RFC 9457 e GlobalExceptionHandler",
)
document.build(story, onFirstPage=draw_cover, onLaterPages=draw_later_page)
print(OUTPUT)
