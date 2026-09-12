package br.com.medflow.exam.definition.application.dto.response;

import br.com.medflow.exam.definition.domain.ExamDefinition;
import br.com.medflow.exam.definition.domain.enums.SampleType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica o mapeamento da entidade para o contrato de saída da API.
 *
 * Este teste é unitário porque utiliza objetos reais e não depende de Spring,
 * JPA ou banco de dados.
 */
class ExamDefinitionResponseDtoTest {

    @Test
    void deveCriarRespostaComOsDadosDaDefinicao() {
        // Arrange: cria uma definição válida com o estado inicial do domínio.
        ExamDefinition definition = new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma completo",
                SampleType.BLOOD
        );

        // Act: converte a entidade para o DTO retornado pela API.
        ExamDefinitionResponseDto response =
                new ExamDefinitionResponseDto(definition);

        // Assert: todos os dados públicos da definição devem ser preservados.
        assertThat(response.id()).isNull();
        assertThat(response.code()).isEqualTo("HEMOGRAMA");
        assertThat(response.name()).isEqualTo("Hemograma completo");
        assertThat(response.sampleType()).isEqualTo(SampleType.BLOOD);
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(definition.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(definition.getUpdatedAt());
    }
}
