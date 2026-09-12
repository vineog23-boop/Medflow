package br.com.medflow.exam.definition.application.dto.request;

import br.com.medflow.exam.definition.domain.enums.SampleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateExamDefinitionRequestDto(
        @NotBlank(message = "Nome do exame deve ser informado")
        @Size(max = 150, message = "Nome do exame deve ter no máximo 150 caracteres")
        String name,

        @NotNull(message = "Tipo de amostra deve ser informado")
        SampleType sampleType
) {
}
