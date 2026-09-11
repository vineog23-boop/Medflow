package br.com.medflow.exam.application;

import java.util.UUID;

public class ExamOrderNotFoundException extends RuntimeException {

    public ExamOrderNotFoundException(UUID id) {
        super("Ordem de exame não encontrada: " + id);
    }
}
