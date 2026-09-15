package br.com.medflow.exam.order.application.dto;

import br.com.medflow.exam.order.application.dto.request.CreateExamRequestDto;
import br.com.medflow.exam.order.application.dto.request.UpdateExamRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExamOrderRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void fecharValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void deveRetornarMensagensEmPortuguesAoValidarCriacao() {
        // Arrange: reúne duas entradas obrigatórias inválidas no mesmo contrato.
        CreateExamRequestDto request = new CreateExamRequestDto(
                null,
                "   ",
                null
        );

        // Act: executa diretamente a Bean Validation, sem inicializar o Spring.
        Set<ConstraintViolation<CreateExamRequestDto>> violations =
                validator.validate(request);

        // Assert: a borda da API deve apresentar mensagens claras em português.
        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder(
                        "Paciente deve ser informado",
                        "Código do exame deve ser informado"
                );
    }

    @Test
    void deveRetornarMensagemEmPortuguesParaCodigoMaiorQueLimiteNaAtualizacao() {
        // Arrange: usa 51 caracteres para ultrapassar a fronteira máxima por uma unidade.
        UpdateExamRequestDto request = new UpdateExamRequestDto(
                UUID.randomUUID(),
                "A".repeat(51),
                null
        );

        // Act: valida o contrato de atualização isoladamente.
        Set<ConstraintViolation<UpdateExamRequestDto>> violations =
                validator.validate(request);

        // Assert: a mensagem expõe o limite correto para quem consome a API.
        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .containsExactly(
                        "Código do exame deve ter no máximo 50 caracteres"
                );
    }
}
