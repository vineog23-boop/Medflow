package br.com.medflow.exam.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica as invariantes aplicadas na criação de uma solicitação de exame.
 *
 * Estes testes são unitários porque avaliam somente o comportamento do domínio,
 * sem depender de Spring, JPA ou banco de dados.
 */
class ExamRequestTest {

    @Test
    void deveCriarSolicitacaoComValoresIniciaisDoDominio() {
        // Arrange: a prioridade não informada deve receber o valor padrão.
        UUID patientId = UUID.randomUUID();

        // Act
        ExamRequest examRequest = new ExamRequest(patientId, "HEMOGRAMA", null);

        // Assert: a entidade deve nascer em um estado válido para o workflow.
        assertThat(examRequest.getPatientId()).isEqualTo(patientId);
        assertThat(examRequest.getExamCode()).isEqualTo("HEMOGRAMA");
        assertThat(examRequest.getPriority()).isEqualTo(ExamPriority.NORMAL);
        assertThat(examRequest.getStatus()).isEqualTo(ExamStatus.RECEIVED);
        assertThat(examRequest.getCreatedAt()).isNotNull();
        assertThat(examRequest.getUpdatedAt()).isEqualTo(examRequest.getCreatedAt());
    }

    @Test
    void devePreservarPrioridadeInformada() {
        // Arrange e Act
        ExamRequest examRequest = new ExamRequest(UUID.randomUUID(), "GLICOSE", ExamPriority.URGENT);

        // Assert
        assertThat(examRequest.getPriority()).isEqualTo(ExamPriority.URGENT);
    }

    @Test
    void naoDeveCriarSolicitacaoSemPaciente() {
        // A ausência do paciente viola uma regra obrigatória do domínio.
        assertThatThrownBy(() -> new ExamRequest(null, "HEMOGRAMA", ExamPriority.NORMAL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoDeveCriarSolicitacaoComCodigoVazio() {
        // O código identifica o exame e não pode ser vazio ou composto apenas por espaços.
        assertThatThrownBy(() -> new ExamRequest(UUID.randomUUID(), "  ", ExamPriority.NORMAL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
