package br.com.medflow.patient.application;

import br.com.medflow.patient.application.dto.PatientDtoRequest;
import br.com.medflow.patient.application.dto.PatientDtoResponse;
import br.com.medflow.patient.application.exception.PatientNotFoundException;
import br.com.medflow.patient.domain.PatientEntity;
import br.com.medflow.patient.persistence.PatientRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica os comportamentos de criação, consulta, atualização e exclusão de pacientes.
 *
 * Estes testes são unitários porque o repository é simulado com Mockito.
 * Assim, validamos a regra da service sem precisar de banco de dados.
 */
class PatientServiceTest {

    private final PatientRepository repository = mock(PatientRepository.class);
    private final PatientService service = new PatientService(repository);

    @Test
    void deveCriarPacienteERetornarDto() {
        // Arrange: o repository devolve a mesma entidade recebida, como se tivesse persistido.
        PatientDtoRequest request = new PatientDtoRequest(
                "Vinícius Oliveira",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );
        when(repository.save(any(PatientEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: cria o paciente por meio da service.
        PatientDtoResponse response = service.create(request);

        // Assert: a borda da aplicação recebe um DTO, com os dados normalizados pela entidade.
        assertThat(response.fullName()).isEqualTo("Vinícius Oliveira");
        assertThat(response.cpf()).isEqualTo("529.982.247-25");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(2000, 1, 15));
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void deveBuscarPacientePorIdERetornarDto() {
        // Arrange: configura um paciente existente.
        UUID id = UUID.randomUUID();
        PatientEntity patient = new PatientEntity(
                "Vinícius Oliveira",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );
        when(repository.findById(id)).thenReturn(Optional.of(patient));

        // Act: busca o paciente pelo identificador.
        PatientDtoResponse response = service.findById(id);

        // Assert: a service não expõe a entidade de persistência.
        assertThat(response.fullName()).isEqualTo("Vinícius Oliveira");
        assertThat(response.cpf()).isEqualTo("529.982.247-25");
    }

    @Test
    void deveAtualizarPacienteERetornarDto() {
        // Arrange: prepara uma entidade existente e os novos dados.
        UUID id = UUID.randomUUID();
        PatientEntity patient = new PatientEntity(
                "Nome Antigo",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );
        PatientDtoRequest request = new PatientDtoRequest(
                "Nome Atualizado",
                "111.444.777-35",
                LocalDate.of(1999, 5, 20)
        );
        when(repository.findById(id)).thenReturn(Optional.of(patient));
        when(repository.save(patient)).thenReturn(patient);

        // Act: atualiza o paciente encontrado.
        PatientDtoResponse response = service.update(id, request);

        // Assert: a entidade foi alterada e o resultado foi convertido para DTO.
        assertThat(response.fullName()).isEqualTo("Nome Atualizado");
        assertThat(response.cpf()).isEqualTo("111.444.777-35");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1999, 5, 20));
        verify(repository).save(patient);
    }

    @Test
    void naoDeveAtualizarPacienteInexistente() {
        // Arrange: o identificador não corresponde a nenhum paciente.
        UUID id = UUID.randomUUID();
        PatientDtoRequest request = new PatientDtoRequest(
                "Nome Atualizado",
                "111.444.777-35",
                LocalDate.of(1999, 5, 20)
        );
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act e Assert: a ausência vira uma exceção conhecida pela aplicação.
        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(PatientNotFoundException.class);
        verify(repository, never()).save(any(PatientEntity.class));
    }

    @Test
    void deveExcluirPacienteExistente() {
        // Arrange: configura o repository para encontrar o paciente informado.
        UUID id = UUID.randomUUID();
        PatientEntity patient = new PatientEntity(
                "Vinícius Oliveira",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );
        when(repository.findById(id)).thenReturn(Optional.of(patient));

        // Act: solicita a exclusão pelo identificador.
        service.delete(id);

        // Assert: a entidade encontrada deve ser enviada para exclusão.
        verify(repository).delete(patient);
    }

    @Test
    void naoDeveExcluirPacienteInexistente() {
        // Arrange: configura o repository para representar um ID inexistente.
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act e Assert: a service deve informar que o paciente não existe.
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(PatientNotFoundException.class);

        // Como nenhum paciente foi encontrado, o repository não pode excluir nada.
        verify(repository, never()).delete(any(PatientEntity.class));
    }
}
