package br.com.medflow.exam.definition.application.dto.response;

import br.com.medflow.exam.definition.domain.ExamDefinition;
import br.com.medflow.exam.definition.domain.enums.SampleType;

import java.time.Instant;
import java.util.UUID;

public record ExamDefinitionResponseDto(
        UUID id,
        String code,
        String name,
        SampleType sampleType,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public ExamDefinitionResponseDto(ExamDefinition examDefinition) {
        this(
                examDefinition.getId(),
                examDefinition.getCode(),
                examDefinition.getName(),
                examDefinition.getSampleType(),
                examDefinition.isActive(),
                examDefinition.getCreatedAt(),
                examDefinition.getUpdatedAt()
        );
    }
}
