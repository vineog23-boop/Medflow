package br.com.medflow.exam.definition.application;

import br.com.medflow.exam.definition.application.dto.request.CreateExamDefinitionRequestDto;
import br.com.medflow.exam.definition.application.dto.request.UpdateExamDefinitionRequestDto;
import br.com.medflow.exam.definition.application.dto.response.ExamDefinitionResponseDto;
import br.com.medflow.exam.definition.application.exception.ExamDefinitionCodeConflictException;
import br.com.medflow.exam.definition.application.exception.ExamDefinitionNotFoundException;
import br.com.medflow.exam.definition.domain.ExamDefinition;
import br.com.medflow.exam.definition.domain.enums.SampleType;
import br.com.medflow.exam.definition.persistence.ExamDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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
 * Testa as regras de aplicação da definição de exame isolando a persistência
 * com um mock do repository.
 */
class ExamDefinitionServiceTest {

    private final ExamDefinitionRepository repository = mock(ExamDefinitionRepository.class);
    private final ExamDefinitionService service = new ExamDefinitionService(repository);

    @Test
    void deveCriarDefinicaoQuandoCodigoNaoEstiverCadastrado() {
        CreateExamDefinitionRequestDto request = new CreateExamDefinitionRequestDto(
                " hemograma ",
                "Hemograma completo",
                SampleType.BLOOD
        );
        when(repository.existsByCode("HEMOGRAMA")).thenReturn(false);
        when(repository.save(any(ExamDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExamDefinitionResponseDto response = service.create(request);

        assertThat(response.code()).isEqualTo("HEMOGRAMA");
        assertThat(response.name()).isEqualTo("Hemograma completo");
        assertThat(response.sampleType()).isEqualTo(SampleType.BLOOD);
        assertThat(response.active()).isTrue();
        verify(repository).existsByCode("HEMOGRAMA");
        verify(repository).save(any(ExamDefinition.class));
    }

    @Test
    void naoDeveCriarDefinicaoComCodigoDuplicado() {
        CreateExamDefinitionRequestDto request = new CreateExamDefinitionRequestDto(
                " hemograma ",
                "Hemograma completo",
                SampleType.BLOOD
        );
        when(repository.existsByCode("HEMOGRAMA")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ExamDefinitionCodeConflictException.class)
                .hasMessage("Já existe uma definição de exame com o código: HEMOGRAMA");

        verify(repository, never()).save(any(ExamDefinition.class));
    }

    @Test
    void deveBuscarDefinicaoPorId() {
        UUID id = UUID.randomUUID();
        ExamDefinition definition = criarDefinicao();
        when(repository.findById(id)).thenReturn(Optional.of(definition));

        ExamDefinitionResponseDto response = service.findById(id);

        assertThat(response.code()).isEqualTo(definition.getCode());
        assertThat(response.name()).isEqualTo(definition.getName());
    }

    @Test
    void deveLancarExcecaoQuandoDefinicaoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ExamDefinitionNotFoundException.class)
                .hasMessage("Definição de exame não encontrada: " + id);
    }

    @Test
    void deveListarDefinicoesComPaginacao() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ExamDefinition> page = new PageImpl<>(
                List.of(criarDefinicao()),
                pageRequest,
                1
        );
        when(repository.findAll(pageRequest)).thenReturn(page);

        Page<ExamDefinitionResponseDto> response = service.findAll(pageRequest);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().code()).isEqualTo("HEMOGRAMA");
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    void deveAtualizarNomeETipoDeAmostra() {
        UUID id = UUID.randomUUID();
        ExamDefinition definition = criarDefinicao();
        UpdateExamDefinitionRequestDto request = new UpdateExamDefinitionRequestDto(
                "Hemograma automatizado",
                SampleType.OTHER
        );
        when(repository.findById(id)).thenReturn(Optional.of(definition));
        when(repository.save(definition)).thenReturn(definition);

        ExamDefinitionResponseDto response = service.update(id, request);

        assertThat(response.code()).isEqualTo("HEMOGRAMA");
        assertThat(response.name()).isEqualTo("Hemograma automatizado");
        assertThat(response.sampleType()).isEqualTo(SampleType.OTHER);
        verify(repository).save(definition);
    }

    @Test
    void naoDeveAtualizarDefinicaoInexistente() {
        UUID id = UUID.randomUUID();
        UpdateExamDefinitionRequestDto request = new UpdateExamDefinitionRequestDto(
                "Hemograma automatizado",
                SampleType.BLOOD
        );
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(ExamDefinitionNotFoundException.class);

        verify(repository, never()).save(any(ExamDefinition.class));
    }

    @Test
    void deveDesativarDefinicao() {
        UUID id = UUID.randomUUID();
        ExamDefinition definition = criarDefinicao();
        when(repository.findById(id)).thenReturn(Optional.of(definition));

        service.deactivate(id);

        assertThat(definition.isActive()).isFalse();
        verify(repository).save(definition);
    }

    @Test
    void naoDeveDesativarDefinicaoInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(id))
                .isInstanceOf(ExamDefinitionNotFoundException.class);

        verify(repository, never()).save(any(ExamDefinition.class));
    }

    private ExamDefinition criarDefinicao() {
        return new ExamDefinition(
                "HEMOGRAMA",
                "Hemograma completo",
                SampleType.BLOOD
        );
    }
}
