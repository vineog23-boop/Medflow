package br.com.medflow.shared.web.error;

import java.net.URI;

/**
 * Catálogo central dos identificadores de problemas expostos pela API.
 *
 * <p>As URNs funcionam como identificadores estáveis do contrato HTTP e não
 * dependem da existência de um domínio público.</p>
 */
public final class ProblemTypes {

    public static final URI PATIENT_NOT_FOUND =
            URI.create("urn:medflow:problem:patient-not-found");
    public static final URI EXAM_ORDER_NOT_FOUND =
            URI.create("urn:medflow:problem:exam-order-not-found");
    public static final URI INVALID_REQUEST =
            URI.create("urn:medflow:problem:invalid-request");
    public static final URI VALIDATION_ERROR =
            URI.create("urn:medflow:problem:validation-error");
    public static final URI INTERNAL_ERROR =
            URI.create("urn:medflow:problem:internal-error");

    private ProblemTypes() {
    }
}
