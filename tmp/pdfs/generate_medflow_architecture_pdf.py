from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
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


ROOT = Path(r"C:\Users\vine\Desktop\Workspace\projetos\Medflow")
OUTPUT = ROOT / "output" / "pdf" / "medflow-arquitetura-e-camadas.pdf"

NAVY = colors.HexColor("#0F2747")
TEAL = colors.HexColor("#188C88")
LIGHT_TEAL = colors.HexColor("#E9F6F5")
SLATE = colors.HexColor("#405166")
PALE = colors.HexColor("#F4F7FA")
LINE = colors.HexColor("#D8E0E8")
ORANGE = colors.HexColor("#BD6A10")
WHITE = colors.white


class ArchitectureDiagram(Flowable):
    """Diagrama simples da organizacao modular sem depender de imagens externas."""

    def __init__(self, width=16.8 * cm, height=12.3 * cm):
        super().__init__()
        self.width = width
        self.height = height

    def draw_box(self, canvas, x, y, width, height, title, text, fill):
        canvas.setFillColor(fill)
        canvas.roundRect(x, y, width, height, 7, fill=1, stroke=0)
        canvas.setStrokeColor(LINE)
        canvas.roundRect(x, y, width, height, 7, fill=0, stroke=1)
        canvas.setFillColor(NAVY)
        canvas.setFont("Helvetica-Bold", 9)
        canvas.drawCentredString(x + width / 2, y + height - 15, title)
        canvas.setFillColor(SLATE)
        canvas.setFont("Helvetica", 7.4)
        for index, line in enumerate(text):
            canvas.drawCentredString(x + width / 2, y + height - 29 - index * 10, line)

    def draw(self):
        canvas = self.canv
        canvas.setFillColor(PALE)
        canvas.roundRect(0, 0, self.width, self.height, 10, fill=1, stroke=0)

        canvas.setFillColor(NAVY)
        canvas.setFont("Helvetica-Bold", 11)
        canvas.drawString(14, self.height - 19, "Monolito modular - desenho atual")
        canvas.setFillColor(SLATE)
        canvas.setFont("Helvetica", 7.5)
        canvas.drawString(14, self.height - 31, "Módulos de negócio organizados em fatias verticais; ainda não são serviços distribuídos.")

        margin = 14
        gap = 9
        box_width = (self.width - margin * 2 - gap) / 2
        box_height = 39
        top = self.height - 84
        self.draw_box(canvas, margin, top, box_width, box_height, "patient", ["domínio do paciente", "application | persistence | web"], LIGHT_TEAL)
        self.draw_box(canvas, margin + box_width + gap, top, box_width, box_height, "exam.order", ["pedido de exame", "application | persistence | web"], LIGHT_TEAL)
        self.draw_box(canvas, margin, top - box_height - 12, box_width, box_height, "exam.definition", ["catálogo de exames", "application | persistence | web"], LIGHT_TEAL)
        self.draw_box(canvas, margin + box_width + gap, top - box_height - 12, box_width, box_height, "shared", ["erros HTTP e preocupações comuns", "sem regra de negócio específica"], colors.HexColor("#F0F3F7"))

        y = 18
        width = self.width - margin * 2
        canvas.setFillColor(WHITE)
        canvas.roundRect(margin, y, width, 31, 7, fill=1, stroke=0)
        canvas.setStrokeColor(TEAL)
        canvas.roundRect(margin, y, width, 31, 7, fill=0, stroke=1)
        canvas.setFillColor(TEAL)
        canvas.setFont("Helvetica-Bold", 8.5)
        canvas.drawCentredString(self.width / 2, y + 19, "Padrão repetido dentro de cada módulo")
        canvas.setFillColor(SLATE)
        canvas.setFont("Helvetica", 8)
        canvas.drawCentredString(self.width / 2, y + 8, "domain  ->  persistence  ->  application  ->  web")


def page_number(canvas, document):
    canvas.saveState()
    canvas.setStrokeColor(LINE)
    canvas.line(document.leftMargin, 1.35 * cm, A4[0] - document.rightMargin, 1.35 * cm)
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(SLATE)
    canvas.drawString(document.leftMargin, 0.9 * cm, "MedFlow - Arquitetura e camadas")
    canvas.drawRightString(A4[0] - document.rightMargin, 0.9 * cm, f"Página {document.page}")
    canvas.restoreState()


def paragraph(text, style):
    return Paragraph(text, style)


def build_pdf():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    doc = SimpleDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        rightMargin=1.65 * cm,
        leftMargin=1.65 * cm,
        topMargin=1.55 * cm,
        bottomMargin=1.8 * cm,
        title="MedFlow - Arquitetura e camadas",
        author="Vinícius Oliveira",
    )

    styles = getSampleStyleSheet()
    title = ParagraphStyle("TitleCustom", parent=styles["Title"], fontName="Helvetica-Bold", fontSize=27, leading=32, textColor=NAVY, spaceAfter=12)
    subtitle = ParagraphStyle("Subtitle", parent=styles["BodyText"], fontName="Helvetica", fontSize=12, leading=17, textColor=SLATE, spaceAfter=15)
    h1 = ParagraphStyle("H1Custom", parent=styles["Heading1"], fontName="Helvetica-Bold", fontSize=18, leading=23, textColor=NAVY, spaceBefore=4, spaceAfter=10)
    h2 = ParagraphStyle("H2Custom", parent=styles["Heading2"], fontName="Helvetica-Bold", fontSize=12.5, leading=16, textColor=TEAL, spaceBefore=10, spaceAfter=6)
    body = ParagraphStyle("BodyCustom", parent=styles["BodyText"], fontName="Helvetica", fontSize=9.4, leading=14, textColor=SLATE, spaceAfter=7)
    small = ParagraphStyle("Small", parent=body, fontSize=8.2, leading=11)
    callout = ParagraphStyle("Callout", parent=body, fontSize=9.2, leading=13, textColor=NAVY)
    code = ParagraphStyle("Code", fontName="Courier", fontSize=8.15, leading=11, textColor=NAVY, backColor=colors.HexColor("#F1F5F8"), borderColor=LINE, borderWidth=0.5, borderPadding=9)
    table_header = ParagraphStyle("TableHeader", parent=small, fontName="Helvetica-Bold", textColor=WHITE, leading=10)
    table_body = ParagraphStyle("TableBody", parent=small, textColor=SLATE, leading=11)

    def info_box(text, tint=LIGHT_TEAL):
        box = Table([[paragraph(text, callout)]], colWidths=[16.8 * cm])
        box.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, -1), tint),
            ("BOX", (0, 0), (-1, -1), 0.7, TEAL),
            ("LEFTPADDING", (0, 0), (-1, -1), 10),
            ("RIGHTPADDING", (0, 0), (-1, -1), 10),
            ("TOPPADDING", (0, 0), (-1, -1), 9),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 9),
        ]))
        return box

    def standard_table(headers, rows, widths):
        data = [[paragraph(header, table_header) for header in headers]]
        data.extend([[paragraph(value, table_body) for value in row] for row in rows])
        table = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
        table.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), NAVY),
            ("BACKGROUND", (0, 1), (-1, -1), WHITE),
            ("GRID", (0, 0), (-1, -1), 0.4, LINE),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 7),
            ("RIGHTPADDING", (0, 0), (-1, -1), 7),
            ("TOPPADDING", (0, 0), (-1, -1), 6),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ("ROWBACKGROUNDS", (0, 1), (-1, -1), [WHITE, PALE]),
        ]))
        return table

    story = []
    story.extend([
        Spacer(1, 2.4 * cm),
        paragraph("MEDFLOW", ParagraphStyle("CoverMark", parent=small, fontName="Helvetica-Bold", fontSize=10, leading=13, textColor=TEAL, alignment=TA_CENTER)),
        Spacer(1, 0.35 * cm),
        paragraph("Arquitetura, camadas e fluxo de implementação", ParagraphStyle("CoverTitle", parent=title, alignment=TA_CENTER, fontSize=25, leading=31)),
        paragraph("Um guia visual do que já foi construído, por que cada camada existe e como evoluir o monólito modular com segurança.", ParagraphStyle("CoverSubtitle", parent=subtitle, alignment=TA_CENTER, fontSize=12, leading=18)),
        Spacer(1, 0.8 * cm),
        info_box("<b>Recorte deste documento:</b> estado do código em 12 de setembro de 2026. O código e os testes são a fonte principal; o README ainda é parcial para a fatia de ExamDefinition."),
        Spacer(1, 1.5 * cm),
        paragraph("IDEIA CENTRAL", ParagraphStyle("CoverLabel", parent=small, fontName="Helvetica-Bold", textColor=TEAL, alignment=TA_CENTER)),
        paragraph("Aprender por fatia vertical: entidade -> repositório -> serviço -> controller, mantendo cada responsabilidade no seu lugar.", ParagraphStyle("CoverIdea", parent=body, alignment=TA_CENTER, fontSize=12, leading=18, textColor=NAVY)),
        Spacer(1, 3.7 * cm),
        paragraph("Vinícius Oliveira  |  Java 21 + Spring Boot 4.1.1", ParagraphStyle("CoverFoot", parent=small, alignment=TA_CENTER, textColor=SLATE)),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("1. Visão do projeto", h1),
        paragraph("O MedFlow é um sistema de gestão de fluxo de exames. A escolha atual é um <b>monólito modular</b>: uma aplicação Spring Boot, um repositório e módulos de negócio separados por pacote. É uma forma adequada de consolidar domínio, API, JPA e testes antes de introduzir a complexidade operacional de microserviços.", body),
        info_box("<b>Monólito modular não é um microserviço disfarçado.</b> Hoje os módulos se chamam diretamente dentro do mesmo processo. A separação por domínio cria fronteiras de código que, se fizer sentido no futuro, podem orientar uma extração gradual."),
        Spacer(1, 0.25 * cm),
        ArchitectureDiagram(),
        Spacer(1, 0.25 * cm),
        paragraph("O pacote <b>shared</b> é reservado para preocupações realmente comuns, como a padronização de erros HTTP. Regra de negócio específica não deve ir para ele apenas para evitar repetição.", body),
        paragraph("Módulos já iniciados", h2),
        standard_table(
            ["Módulo", "Responsabilidade atual", "Situação"],
            [
                ["patient", "Cadastro e manutenção de pacientes.", "Fatia vertical já existente."],
                ["exam.order", "Pedido de exames vinculado ao paciente.", "Fatia existente; há reorganização local de DTOs em andamento."],
                ["exam.definition", "Catálogo de tipos de exame: código, nome, amostra e status.", "Entidade, repository, service, controller e testes implementados."],
                ["shared.web.error", "Respostas de erro em formato ProblemDetail.", "Já atende erros de paciente/pedido e validações gerais."],
            ],
            [3.1 * cm, 8.0 * cm, 5.7 * cm],
        ),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("2. O papel de cada camada", h1),
        paragraph("A divisão não é burocracia: cada camada protege uma decisão diferente. Quando uma regra está no lugar certo, o controller permanece fino, o serviço concentra o caso de uso e a entidade protege o domínio mesmo fora da API.", body),
        standard_table(
            ["Camada", "Pergunta que responde", "Exemplos no MedFlow"],
            [
                ["domain", "O objeto pode existir neste estado?", "ExamDefinition, SampleType, validações do construtor, update e deactivate."],
                ["persistence", "Como consultar ou gravar?", "ExamDefinitionRepository e os mapeamentos JPA."],
                ["application", "Como executar o caso de uso?", "ExamDefinitionService; DTOs de entrada e de saída; exceções de negócio."],
                ["web", "Como expor o caso de uso em HTTP?", "ExamDefinitionController, @Valid, status HTTP e paginação."],
                ["shared", "Qual regra técnica é comum a vários módulos?", "GlobalExceptionHandler, ProblemTypes e estrutura de erro."],
            ],
            [2.6 * cm, 5.6 * cm, 8.6 * cm],
        ),
        paragraph("Validação em três fronteiras", h2),
        standard_table(
            ["Onde", "O que protege", "Exemplo"],
            [
                ["DTO de entrada", "Formato do HTTP recebido antes do caso de uso.", "@NotBlank, @Size e @Valid no controller."],
                ["Entidade", "Invariantes do domínio em qualquer ponto de criação/alteração.", "Código do exame normalizado; nome, sampleType e limites verificados."],
                ["Coluna JPA", "Contrato de persistência quando houver banco configurado.", "unique, nullable, length e @Version no mapeamento."],
            ],
            [3.0 * cm, 6.7 * cm, 7.1 * cm],
        ),
        info_box("As validações se complementam. A anotação no DTO melhora a resposta para o cliente; a entidade impede estado inválido por qualquer entrada; a coluna protege o armazenamento. Uma não substitui a outra.", colors.HexColor("#FFF6E8")),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("3. Fluxo de uma requisição", h1),
        paragraph("O caminho abaixo usa a criação de uma definição de exame. A mesma ideia organiza as outras fatias: a borda adapta HTTP; o serviço orquestra; o domínio valida seu estado; o repository representa a persistência; o DTO de resposta evita expor a entidade.", body),
        Preformatted("POST /exam-definitions\n    JSON -> @Valid no Controller\n         -> ExamDefinitionService.create(request)\n         -> new ExamDefinition(code, name, sampleType)\n         -> repository.existsByCode(code) / repository.save(entity)\n         -> ExamDefinitionResponseDto\n         -> HTTP 201 Created", code),
        Spacer(1, 0.28 * cm),
        standard_table(
            ["Etapa", "Responsabilidade", "Por que importa"],
            [
                ["Controller", "Recebe o contrato HTTP e delega.", "Não deve conter regra de negócio ou acesso direto ao repository."],
                ["Service", "Cria a entidade, checa código duplicado, salva e monta a resposta.", "É o local visível do caso de uso e das regras que envolvem mais de um objeto."],
                ["Entity", "Normaliza e valida dados no construtor.", "Mesmo que alguém crie o objeto fora da API, a regra continua protegida."],
                ["Repository", "Oferece existsByCode, save, findById e findAll.", "Evita espalhar detalhes de JPA pela aplicação."],
                ["Response DTO", "Expõe apenas dados definidos no contrato de saída.", "Evita acoplamento entre API e entidade de persistência."],
            ],
            [2.55 * cm, 7.0 * cm, 7.25 * cm],
        ),
        paragraph("Erro e resposta", h2),
        paragraph("Erros HTTP são padronizados no <b>GlobalExceptionHandler</b> com ProblemDetail. Já há resposta consistente para validação (422), JSON inválido (400), paciente não encontrado e pedido não encontrado (404). Para a fatia de ExamDefinition, as classes de exceção já existem; o mapeamento específico de <b>não encontrado (404)</b> e <b>código duplicado (409)</b> ainda é o próximo ajuste de integração.", body),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("4. Fatia vertical: ExamDefinition", h1),
        paragraph("ExamDefinition representa o catálogo de exames que podem ser solicitados. Ele não é o pedido de um paciente: é a definição reutilizável de um tipo de exame, por exemplo, um hemograma com código próprio e tipo de amostra.", body),
        standard_table(
            ["Elemento", "Decisão implementada", "Intenção"],
            [
                ["code", "Obrigatório, único, até 50 caracteres, convertido para maiúsculas com trim.", "Tornar o identificador estável; ' hem01 ' e 'HEM01' representam o mesmo código."],
                ["name", "Obrigatório, até 150 caracteres, com trim.", "Evitar rótulos vazios e espaços acidentais."],
                ["sampleType", "Enum obrigatório: BLOOD, URINE, STOOL, SWAB ou OTHER.", "Evitar texto livre para uma classificação controlada."],
                ["active", "Começa ativo; deactivate altera o estado sem apagar o registro.", "Preserva histórico e possibilita soft delete."],
                ["timestamps e version", "createdAt, updatedAt e @Version presentes na entidade.", "Preparam auditoria e concorrência otimista quando JPA/banco estiverem configurados."],
            ],
            [3.1 * cm, 7.2 * cm, 6.5 * cm],
        ),
        paragraph("Contratos de aplicação", h2),
        standard_table(
            ["Objeto", "Uso"],
            [
                ["CreateExamDefinitionRequestDto", "Entrada de criação: code, name e sampleType, com validação de borda."],
                ["UpdateExamDefinitionRequestDto", "Entrada de alteração: name e sampleType. O código não é alterado."],
                ["ExamDefinitionResponseDto", "Saída formatada da API a partir da entidade."],
                ["ExamDefinitionNotFoundException", "Expressa que o id consultado/alterado não existe."],
                ["ExamDefinitionCodeConflictException", "Expressa conflito de unicidade quando o código já existe."],
            ],
            [6.2 * cm, 10.6 * cm],
        ),
        info_box("A service também possui <b>deactivate(id)</b>, mas não há rota HTTP para ela por decisão de escopo: a API atual possui apenas leitura, criação e atualização. Isso evita introduzir PATCH antes de precisar de um contrato claro para a operação.", colors.HexColor("#FFF6E8")),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("5. API e verificação", h1),
        paragraph("A API de definições de exames foi desenhada com quatro operações simples e consistentes com os módulos já existentes.", body),
        standard_table(
            ["Método e rota", "Caso de uso", "Resposta normal"],
            [
                ["GET /exam-definitions", "Lista definições, com paginação Spring.", "200 OK + Page<ExamDefinitionResponseDto>"],
                ["GET /exam-definitions/{id}", "Busca uma definição pelo identificador.", "200 OK + DTO"],
                ["POST /exam-definitions", "Cria uma definição nova.", "201 Created + DTO"],
                ["PUT /exam-definitions/{id}", "Atualiza nome e tipo de amostra.", "200 OK + DTO"],
            ],
            [5.2 * cm, 6.4 * cm, 5.2 * cm],
        ),
        paragraph("Como os testes foram pensados", h2),
        standard_table(
            ["Nível", "Foco", "Por que esse tipo de teste"],
            [
                ["Domínio", "Construtor, normalização, update, deactivate e regras de validade.", "Confirma as invariantes sem depender de Spring ou banco."],
                ["Service", "Orquestração, conflito de código, busca e atualização usando repository mockado.", "Testa caso de uso de modo rápido e isolado."],
                ["DTO", "Anotações de Bean Validation nas entradas e construção da resposta.", "Garante que o contrato da borda rejeite dados inválidos."],
                ["Controller", "Rotas, status, delegação, paginação e POST inválido com 422 via MockMvc.", "Valida a integração da camada web sem iniciar infraestrutura externa."],
            ],
            [2.8 * cm, 7.2 * cm, 6.8 * cm],
        ),
        info_box("<b>Última validação registrada:</b> a suíte completa Maven executou 78 testes, com 0 falhas e 0 erros, antes do commit do controller em 12/09/2026. Isso confirma o comportamento coberto pelos testes; não confirma infraestrutura ainda não configurada."),
        paragraph("Convenção de teste importante: teste não é só uma etapa final. Ele registra o comportamento esperado de uma camada e permite alterar o projeto com segurança depois.", body),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("6. Decisões e evolução arquitetural", h1),
        paragraph("O projeto tem dependências e intenção de evoluir para integrações distribuídas, mas a arquitetura atual conscientemente mantém essa complexidade fora da fatia de domínio. A ordem de aprendizado é: consolidar casos de uso e fronteiras; depois integrar; por último decidir se extrair serviços vale o custo.", body),
        standard_table(
            ["Tema", "Estado atual", "Evolução consciente"],
            [
                ["Banco e migrations", "Entidades e repositories JPA existem; datasource e migrations reais não foram validados.", "Configurar PostgreSQL/Flyway e testar a persistência de verdade."],
                ["Spring Security", "A dependência está presente; a aplicação ainda usa a senha automática de desenvolvimento.", "Definir autenticação, autorização, usuários/roles e regras por rota."],
                ["Mensageria", "RabbitMQ não participa das operações atuais.", "Publicar eventos de domínio quando existir um consumidor real e contrato definido."],
                ["Comunicação síncrona", "Não há OpenFeign em uso dentro do monólito.", "Só faz sentido após a extração de uma fronteira para outro processo."],
                ["API Gateway", "Não existe gateway em execução.", "Introduzir ao expor múltiplos serviços externos, não como passagem interna do monólito."],
            ],
            [3.1 * cm, 6.8 * cm, 6.9 * cm],
        ),
        paragraph("Quando usar comunicação interna e assíncrona?", h2),
        standard_table(
            ["Situação", "Escolha adequada", "Exemplo futuro"],
            [
                ["Mesmo monólito", "Chamada direta entre services, respeitando interfaces e domínio.", "ExamOrder consulta uma definição por uma porta de aplicação, sem HTTP para si mesmo."],
                ["Dado necessário na resposta imediata entre serviços", "HTTP/OpenFeign depois de extrair um serviço.", "Order Service precisa validar uma definição mantida em Laboratory Service."],
                ["Fato que outros módulos podem consumir sem bloquear o fluxo", "Evento assíncrono em RabbitMQ/Kafka.", "Pedido criado -> Notification Service envia uma notificação."],
            ],
            [4.8 * cm, 6.0 * cm, 6.0 * cm],
        ),
        info_box("Regra prática: não criar mensageria ou OpenFeign apenas porque são tecnologias desejadas. Primeiro defina o fato de negócio, o dono do dado, o consumidor e a necessidade de consistência/tempo de resposta.", colors.HexColor("#FFF6E8")),
    ])
    story.append(PageBreak())

    story.extend([
        paragraph("7. Mapa de estado e próximo passo", h1),
        paragraph("Este quadro evita confundir intenção arquitetural com capacidade disponível. Ele é útil para decidir o próximo código sem antecipar infraestrutura que ainda não tem caso de uso definido.", body),
        standard_table(
            ["Já construído", "Pendente de integração", "Planejado para evolução"],
            [
                ["Fatias patient, exam.order e exam.definition.", "Tratador global específico para ExamDefinition: 404 e 409.", "PostgreSQL, Flyway e migrations novas."],
                ["Entidades com regras e mapeamentos JPA.", "Endpoint de desativação, se o contrato HTTP for definido.", "Spring Security com autenticação e autorização."],
                ["Repositories, services, DTOs e controllers de ExamDefinition.", "Persistência real e testes de integração com banco.", "Eventos assíncronos, contratos e consumidores."],
                ["ProblemDetail para erros já conectados ao handler.", "Atualização do README para incluir ExamDefinition.", "Extração gradual de módulos para serviços, se justificada."],
                ["Testes de domínio, service, DTO e web; 78 testes passaram na última execução registrada.", "Padronizar a reorganização de DTOs que está em andamento em exam.order.", "OpenFeign e API Gateway somente após fronteiras externas reais."],
            ],
            [5.6 * cm, 5.6 * cm, 5.6 * cm],
        ),
        paragraph("Próxima sequência recomendada", h2),
        standard_table(
            ["Passo", "Objetivo"],
            [
                ["1. Fechar erros de ExamDefinition", "Adicionar os tipos e handlers 404/409, com testes de web. Fecha a fatia já exposta."],
                ["2. Definir persistência real", "Escolher a configuração local de PostgreSQL e criar migrations novas, sem alterar migrations aplicadas."],
                ["3. Modelar segurança", "Definir quem são os usuários, quais perfis existem e quais operações cada perfil pode realizar."],
                ["4. Avançar no próximo caso de uso", "Repetir o fluxo entidade -> repository -> service -> controller, sempre explicando o papel de cada decisão."],
            ],
            [5.3 * cm, 11.5 * cm],
        ),
        Spacer(1, 0.35 * cm),
        info_box("<b>Resumo:</b> o MedFlow já tem uma base modular concreta. O próximo ganho não é adicionar mais tecnologia; é completar o contrato de erro da fatia atual e consolidar a persistência real. A partir daí, as escolhas de segurança e integração terão um domínio mais estável para apoiar.", LIGHT_TEAL),
        Spacer(1, 1.0 * cm),
        paragraph("Documento de apoio ao desenvolvimento - não substitui o código, os testes e as decisões registradas no repositório.", ParagraphStyle("End", parent=small, alignment=TA_CENTER, textColor=SLATE)),
    ])

    doc.build(story, onFirstPage=page_number, onLaterPages=page_number)
    print(OUTPUT)


if __name__ == "__main__":
    build_pdf()
