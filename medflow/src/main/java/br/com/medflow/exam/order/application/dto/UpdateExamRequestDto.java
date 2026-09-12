package br.com.medflow.exam.order.application.dto;

import br.com.medflow.exam.order.domain.enums.ExamPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateExamRequestDto(
        @NotNull(message = "Paciente deve ser informado") UUID patientId,
        @NotBlank(message = "Código do exame deve ser informado")
        @Size(max = 50, message = "Código do exame deve ter no máximo 50 caracteres")
        String examCode,
        ExamPriority priority
) {
}
