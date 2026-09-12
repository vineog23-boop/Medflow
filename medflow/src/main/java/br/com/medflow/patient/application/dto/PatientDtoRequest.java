package br.com.medflow.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record PatientDtoRequest(
        @NotBlank(message = "Nome completo deve ser informado")
        @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres")
        String fullName,

        @NotBlank(message = "CPF deve ser informado")
        @CPF(message = "CPF deve ser válido")
        String cpf,

        @NotNull(message = "Data de nascimento deve ser informada")
        @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
        LocalDate birthDate
) {
}
