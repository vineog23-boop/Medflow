package br.com.medflow.exam.definition.domain;

import br.com.medflow.exam.definition.domain.enums.SampleType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica as invariantes aplicadas na criação de uma definição de exame.
 *
 * Estes testes são unitários porque exercitam somente a entidade,
 * sem inicializar Spring, JPA ou banco de dados.
 */
class ExamDefinitionTest {

    @Test
    void deveCriarDefinicaoComDadosNormalizadosEEstadoInicial() {
        ExamDefinition definition = new ExamDefinition(
                "  hemograma  ",
                "  Hemograma completo  ",
                SampleType.BLOOD
        );

        assertThat(definition.getCode()).isEqualTo("HEMOGRAMA");
        assertThat(definition.getName()).isEqualTo("Hemograma completo");
        assertThat(definition.getSampleType()).isEqualTo(SampleType.BLOOD);
        assertThat(definition.isActive()).isTrue();
        assertThat(definition.getCreatedAt()).isNotNull();
        assertThat(definition.getUpdatedAt()).isEqualTo(definition.getCreatedAt());
    }

    @Test
    void naoDeveCriarDefinicaoComCodigoVazio() {
        assertThatThrownBy(() -> new ExamDefinition(
                "   ",
                "Hemograma",
                SampleType.BLOOD
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código do exame deve ser informado");
    }

    @Test
    void naoDeveCriarDefinicaoComCodigoMaiorQueCinquentaCaracteres() {
        assertThatThrownBy(() -> new ExamDefinition(
                "A".repeat(51),
                "Hemograma",
                SampleType.BLOOD
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código do exame deve ter no máximo 50 caracteres");
    }

    @Test
    void naoDeveCriarDefinicaoComNomeVazio() {
        assertThatThrownBy(() -> new ExamDefinition(
                "HEMOGRAMA",
                "   ",
                SampleType.BLOOD
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do exame deve ser informado");
    }

    @Test
    void naoDeveCriarDefinicaoComNomeMaiorQueCentoECinquentaCaracteres() {
        assertThatThrownBy(() -> new ExamDefinition(
                "HEMOGRAMA",
                "A".repeat(151),
                SampleType.BLOOD
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do exame deve ter no máximo 150 caracteres");
    }

    @Test
    void naoDeveCriarDefinicaoSemTipoDeAmostra() {
        assertThatThrownBy(() -> new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de amostra deve ser informado");
    }

    @Test
    void deveAtualizarNomeETipoSemAlterarCodigo() {
        ExamDefinition definition = new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma",
                SampleType.BLOOD
        );

        definition.update("  Hemograma completo  ", SampleType.OTHER);

        assertThat(definition.getCode()).isEqualTo("HEMOGRAMA");
        assertThat(definition.getName()).isEqualTo("Hemograma completo");
        assertThat(definition.getSampleType()).isEqualTo(SampleType.OTHER);
        assertThat(definition.getUpdatedAt())
                .isAfterOrEqualTo(definition.getCreatedAt());
    }

    @Test
    void naoDeveAtualizarDefinicaoComNomeVazio() {
        ExamDefinition definition = new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma",
                SampleType.BLOOD
        );

        assertThatThrownBy(() -> definition.update("   ", SampleType.BLOOD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do exame deve ser informado");
    }

    @Test
    void naoDeveAtualizarDefinicaoSemTipoDeAmostra() {
        ExamDefinition definition = new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma",
                SampleType.BLOOD
        );

        assertThatThrownBy(() -> definition.update("Hemograma completo", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de amostra deve ser informado");
    }

    @Test
    void deveDesativarDefinicaoDeExame() {
        // Arrange: toda definição nasce ativa e disponível para novos pedidos.
        ExamDefinition definition = new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma",
                SampleType.BLOOD
        );

        // Act: desativa a definição por meio do comportamento do domínio.
        definition.deactivate();

        // Assert: confirma a mudança de estado e a atualização do registro.
        assertThat(definition.isActive()).isFalse();
        assertThat(definition.getUpdatedAt())
                .isAfterOrEqualTo(definition.getCreatedAt());
    }

    @Test
    void devePermitirDesativarDefinicaoQueJaEstaInativa() {
        // Arrange: cria a definição e realiza a primeira desativação.
        ExamDefinition definition = new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma",
                SampleType.BLOOD
        );

        definition.deactivate();

        // Act: repete a operação para verificar que ela é idempotente.
        definition.deactivate();

        // Assert: chamadas repetidas mantêm o mesmo estado sem lançar exceção.
        assertThat(definition.isActive()).isFalse();
    }
}
