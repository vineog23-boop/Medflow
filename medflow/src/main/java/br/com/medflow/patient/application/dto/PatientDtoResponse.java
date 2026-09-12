package br.com.medflow.patient.application.dto;

import br.com.medflow.patient.domain.PatientEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientDtoResponse(
        UUID id,
        String fullName,
        String cpf,
        LocalDate birthDate,
        Instant createdAt
) {

    public PatientDtoResponse(PatientEntity entity) {
        this(
                entity.getId(),
                entity.getFullName(),
                entity.getCpf(),
                entity.getBirthDate(),
                entity.getCreatedAt()
        );
    }
}
