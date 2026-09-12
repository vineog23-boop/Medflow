package br.com.medflow.exam.order.application.dto;

import br.com.medflow.exam.order.domain.ExamOrder;
import br.com.medflow.exam.order.domain.enums.ExamPriority;
import br.com.medflow.exam.order.domain.enums.ExamStatus;

import java.time.Instant;
import java.util.UUID;

public record ExamOrderResponseDto(
        UUID id,
        UUID patientId,
        String examCode,
        ExamPriority priority,
        ExamStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public ExamOrderResponseDto(ExamOrder examOrder) {
        this(
                examOrder.getId(),
                examOrder.getPatientId(),
                examOrder.getExamCode(),
                examOrder.getPriority(),
                examOrder.getStatus(),
                examOrder.getCreatedAt(),
                examOrder.getUpdatedAt()
        );
    }
}
