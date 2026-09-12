package br.com.medflow.patient.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica as regras aplicadas durante a criação de um paciente.
 *
 * Estes testes são unitários porque exercitam somente a entidade,
 * sem inicializar Spring, JPA ou banco de dados.
 */
class PatientEntityTest {

    @Test
    void deveCriarPacienteComDadosValidos() {
        // Arrange: prepara os dados necessários para cadastrar um paciente.
        String fullName = "Vinícius Oliveira";
        String cpf = "529.982.247-25";
        LocalDate birthDate = LocalDate.of(2000, 1, 15);

        // Act: cria a entidade usando o construtor de negócio.
        PatientEntity patient = new PatientEntity(fullName, cpf, birthDate);

        // Assert: confirma que a entidade nasceu com os dados e datas esperados.
        assertThat(patient.getFullName()).isEqualTo(fullName);
        assertThat(patient.getCpf()).isEqualTo(cpf);
        assertThat(patient.getBirthDate()).isEqualTo(birthDate);
        assertThat(patient.getCreatedAt()).isNotNull();
        assertThat(patient.getUpdatedAt()).isEqualTo(patient.getCreatedAt());
        assertThat(patient.getId()).isNull();
        assertThat(patient.getVersion()).isNull();
    }

    @Test
    void deveRemoverEspacosDasExtremidadesDoNomeEDoCpf() {
        // Arrange e Act: envia textos com espaços antes e depois do conteúdo.
        PatientEntity patient = new PatientEntity(
                "  Vinícius Oliveira  ",
                "  529.982.247-25  ",
                LocalDate.of(2000, 1, 15)
        );

        // Assert: trim remove somente os espaços das extremidades.
        assertThat(patient.getFullName()).isEqualTo("Vinícius Oliveira");
        assertThat(patient.getCpf()).isEqualTo("529.982.247-25");
    }

    @Test
    void naoDeveCriarPacienteSemNome() {
        // Act e Assert: nome nulo viola uma regra obrigatória do domínio.
        assertThatThrownBy(() -> new PatientEntity(
                null,
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome completo deve ser informado");
    }

    @Test
    void naoDeveCriarPacienteComNomeEmBranco() {
        // Act e Assert: espaços não representam um nome válido.
        assertThatThrownBy(() -> new PatientEntity(
                "   ",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome completo deve ser informado");
    }

    @Test
    void naoDeveCriarPacienteSemCpf() {
        // Act e Assert: o CPF é obrigatório para criar o paciente.
        assertThatThrownBy(() -> new PatientEntity(
                "Vinícius Oliveira",
                null,
                LocalDate.of(2000, 1, 15)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF deve ser informado");
    }

    @Test
    void naoDeveCriarPacienteComCpfEmBranco() {
        // Act e Assert: espaços não representam um CPF válido.
        assertThatThrownBy(() -> new PatientEntity(
                "Vinícius Oliveira",
                "   ",
                LocalDate.of(2000, 1, 15)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF deve ser informado");
    }

    @Test
    void naoDeveCriarPacienteSemDataDeNascimento() {
        // Act e Assert: todo paciente precisa possuir uma data de nascimento.
        assertThatThrownBy(() -> new PatientEntity(
                "Vinícius Oliveira",
                "529.982.247-25",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de nascimento deve ser informada");
    }

    @Test
    void naoDeveCriarPacienteComDataDeNascimentoFutura() {
        // Arrange: amanhã sempre representa uma data futura.
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // Act e Assert: uma pessoa não pode nascer no futuro.
        assertThatThrownBy(() -> new PatientEntity(
                "Vinícius Oliveira",
                "529.982.247-25",
                futureDate
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de nascimento não pode estar no futuro");
    }

    @Test
    void deveAtualizarOsDadosDoPaciente() {
        // Arrange: cria um paciente e guarda o instante original de atualização.
        PatientEntity patient = new PatientEntity(
                "Nome Antigo",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );
        Instant previousUpdatedAt = patient.getUpdatedAt();

        // Act: aplica os novos dados por meio do comportamento da própria entidade.
        patient.update(
                "  Nome Atualizado  ",
                "  111.444.777-35  ",
                LocalDate.of(1999, 5, 20)
        );

        // Assert: os dados mudam, os textos são normalizados e a criação é preservada.
        assertThat(patient.getFullName()).isEqualTo("Nome Atualizado");
        assertThat(patient.getCpf()).isEqualTo("111.444.777-35");
        assertThat(patient.getBirthDate()).isEqualTo(LocalDate.of(1999, 5, 20));
        assertThat(patient.getCreatedAt()).isNotNull();
        assertThat(patient.getUpdatedAt()).isAfterOrEqualTo(previousUpdatedAt);
    }

    @Test
    void naoDeveAtualizarPacienteComNomeEmBranco() {
        // Arrange: cria uma entidade válida antes de tentar uma alteração inválida.
        PatientEntity patient = new PatientEntity(
                "Vinícius Oliveira",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );

        // Act e Assert: a mesma regra do cadastro também protege a atualização.
        assertThatThrownBy(() -> patient.update(
                "   ",
                "111.444.777-35",
                LocalDate.of(1999, 5, 20)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome completo deve ser informado");
    }
}
