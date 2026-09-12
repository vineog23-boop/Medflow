package br.com.medflow.exam.definition.application;

import br.com.medflow.exam.definition.application.dto.request.CreateExamDefinitionRequestDto;
import br.com.medflow.exam.definition.application.dto.request.UpdateExamDefinitionRequestDto;
import br.com.medflow.exam.definition.application.dto.response.ExamDefinitionResponseDto;
import br.com.medflow.exam.definition.application.exception.ExamDefinitionCodeConflictException;
import br.com.medflow.exam.definition.application.exception.ExamDefinitionNotFoundException;
import br.com.medflow.exam.definition.domain.ExamDefinition;
import br.com.medflow.exam.definition.persistence.ExamDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExamDefinitionService {

    private final ExamDefinitionRepository examDefinitionRepository;

    public ExamDefinitionService(ExamDefinitionRepository examDefinitionRepository) {
        this.examDefinitionRepository = examDefinitionRepository;
    }

    /**
     * Cria uma definição de exame sem permitir duplicidade de código no catálogo.
     *
     * <p>O código é normalizado pela entidade antes da verificação. Portanto,
     * "hemograma" e " HEMOGRAMA " são considerados o mesmo exame.</p>
     *
     * @param request dados necessários para cadastrar a definição
     * @return dados da definição criada
     * @throws ExamDefinitionCodeConflictException quando o código já está cadastrado
     */
    public ExamDefinitionResponseDto create(CreateExamDefinitionRequestDto request) {
        ExamDefinition definition = new ExamDefinition(
                request.code(),
                request.name(),
                request.sampleType()
        );

        if (examDefinitionRepository.existsByCode(definition.getCode())) {
            throw new ExamDefinitionCodeConflictException(definition.getCode());
        }

        ExamDefinition savedDefinition = examDefinitionRepository.save(definition);
        return new ExamDefinitionResponseDto(savedDefinition);
    }

    public ExamDefinitionResponseDto findById(UUID id) {
        ExamDefinition definition = examDefinitionRepository.findById(id)
                .orElseThrow(() -> new ExamDefinitionNotFoundException(id));

        return new ExamDefinitionResponseDto(definition);
    }

    public Page<ExamDefinitionResponseDto> findAll(Pageable pageable) {
        return examDefinitionRepository.findAll(pageable)
                .map(ExamDefinitionResponseDto::new);
    }

    public ExamDefinitionResponseDto update(UUID id, UpdateExamDefinitionRequestDto request) {
        ExamDefinition definition = examDefinitionRepository.findById(id)
                .orElseThrow(() -> new ExamDefinitionNotFoundException(id));

        definition.update(
                request.name(),
                request.sampleType()
        );

        ExamDefinition updatedDefinition = examDefinitionRepository.save(definition);
        return new ExamDefinitionResponseDto(updatedDefinition);
    }

    /**
     * Desativa uma definição de exame sem removê-la do banco de dados.
     *
     * @param id identificador da definição que será desativada
     * @throws ExamDefinitionNotFoundException quando a definição não existe
     */
    public void deactivate(UUID id) {
        ExamDefinition definition = examDefinitionRepository.findById(id)
                .orElseThrow(() -> new ExamDefinitionNotFoundException(id));

        definition.deactivate();
        examDefinitionRepository.save(definition);
    }
}
