package br.com.medflow.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_requests")
public class ExamRequest {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    @NotNull
    private UUID patientId;

    @Column(name = "exam_code", nullable = false, length = 50)
    @NotBlank
    private String examCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @NotNull
    private ExamPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @NotNull
    private ExamStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected ExamRequest() {
        // Construtor usado pelo JPA para reconstruir registros do banco.
    }

    public ExamRequest(UUID patientId, String examCode, ExamPriority priority) {
        if (patientId == null) {
            throw new IllegalArgumentException("Paciente deve ser informado");
        }

        if (examCode == null || examCode.isBlank()) {
            throw new IllegalArgumentException("Código do exame deve ser informado");
        }

        this.patientId = patientId;
        this.examCode = examCode.trim();

        if (priority == null) {
            this.priority = ExamPriority.NORMAL;
        } else {
            this.priority = priority;
        }

        this.status = ExamStatus.RECEIVED;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getExamCode() {
        return examCode;
    }

    public ExamPriority getPriority() {
        return priority;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
