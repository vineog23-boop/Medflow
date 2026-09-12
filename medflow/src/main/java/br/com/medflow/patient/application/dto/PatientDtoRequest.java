package br.com.medflow.patient.application.dto;

import java.time.LocalDate;

public record PatientDtoRequest(
        String fullName,
        String cpf,
        LocalDate birthDate
) {
}
