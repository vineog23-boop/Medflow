package br.com.medflow.exam.domain;

import br.com.medflow.exam.domain.enums.ExamPriority;
import br.com.medflow.exam.domain.enums.ExamStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica as invariantes aplicadas na criação de uma ordem de exame.
 *
 * Estes testes são unitários porque avaliam somente o comportamento do domínio,
 * sem depender de Spring, JPA ou banco de dados.
 */
class ExamOrderTest {

    @Test
    void deveCriarOrdemComValoresIniciaisDoDominio() {
        // Arrange: a prioridade não informada deve receber o valor padrão.
        UUID patientId = UUID.randomUUID();

        // Act
        ExamOrder examOrder = new ExamOrder(patientId, "HEMOGRAMA", null);

        // Assert: a entidade deve nascer em um estado válido para o workflow.
        assertThat(examOrder.getPatientId()).isEqualTo(patientId);
        assertThat(examOrder.getExamCode()).isEqualTo("HEMOGRAMA");
        assertThat(examOrder.getPriority()).isEqualTo(ExamPriority.NORMAL);
        assertThat(examOrder.getStatus()).isEqualTo(ExamStatus.RECEIVED);
        assertThat(examOrder.getCreatedAt()).isNotNull();
        assertThat(examOrder.getUpdatedAt()).isEqualTo(examOrder.getCreatedAt());
    }

    @Test
    void devePreservarPrioridadeInformada() {
        // Arrange e Act
        ExamOrder examOrder = new ExamOrder(UUID.randomUUID(), "GLICOSE", ExamPriority.URGENT);

        // Assert
        assertThat(examOrder.getPriority()).isEqualTo(ExamPriority.URGENT);
    }

    @Test
    void naoDeveCriarOrdemSemPaciente() {
        // A ausência do paciente viola uma regra obrigatória do domínio.
        assertThatThrownBy(() -> new ExamOrder(null, "HEMOGRAMA", ExamPriority.NORMAL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoDeveCriarOrdemComCodigoVazio() {
        // O código identifica o exame e não pode ser vazio ou composto apenas por espaços.
        assertThatThrownBy(() -> new ExamOrder(UUID.randomUUID(), "  ", ExamPriority.NORMAL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
