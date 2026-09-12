package br.com.medflow.patient.application.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID id) {
        super("Paciente com id: " + id + " não encontrado");
    }
}
