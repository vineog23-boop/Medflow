package br.com.medflow.exam.definition.application.dto;

import br.com.medflow.exam.definition.application.dto.request.CreateExamDefinitionRequestDto;
import br.com.medflow.exam.definition.application.dto.request.UpdateExamDefinitionRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida os contratos de entrada sem carregar o contexto do Spring.
 */
class ExamDefinitionRequestValidationTest {

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
    void deveValidarCamposObrigatoriosNaCriacao() {
        CreateExamDefinitionRequestDto request = new CreateExamDefinitionRequestDto(
                "   ",
                "   ",
                null
        );

        Set<ConstraintViolation<CreateExamDefinitionRequestDto>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Código do exame deve ser informado",
                        "Nome do exame deve ser informado",
                        "Tipo de amostra deve ser informado"
                );
    }

    @Test
    void deveValidarNomeETipoDeAmostraNaAtualizacao() {
        UpdateExamDefinitionRequestDto request = new UpdateExamDefinitionRequestDto(
                "A".repeat(151),
                null
        );

        Set<ConstraintViolation<UpdateExamDefinitionRequestDto>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Nome do exame deve ter no máximo 150 caracteres",
                        "Tipo de amostra deve ser informado"
                );
    }
}
