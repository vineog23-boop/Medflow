package br.com.medflow.exam.application.dto;

import br.com.medflow.exam.domain.enums.ExamPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateExamRequestDto(
        @NotNull UUID patientId,
        @NotBlank @Size(max = 50) String examCode,
        ExamPriority priority
) {
}
