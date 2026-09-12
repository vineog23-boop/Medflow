package br.com.medflow.exam.definition.application.exception;

public class ExamDefinitionCodeConflictException extends RuntimeException {

    public ExamDefinitionCodeConflictException(String code) {
        super("Já existe uma definição de exame com o código: " + code);
    }
}
