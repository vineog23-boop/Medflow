package br.com.medflow.exam.domain;

import br.com.medflow.exam.domain.enums.SampleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "exam_definitions")
public class ExamDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, updatable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", nullable = false, length = 30)
    private SampleType sampleType;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected ExamDefinition() {
    }

    public ExamDefinition(
            String code,
            String name,
            SampleType sampleType) {

        validateCode(code);
        validateNameAndSampleType(name, sampleType);

        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.sampleType = sampleType;
        this.active = true;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name, SampleType sampleType) {
        validateNameAndSampleType(name, sampleType);

        this.name = name.trim();
        this.sampleType = sampleType;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }

        this.active = false;
        this.updatedAt = Instant.now();
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Código do exame deve ser informado");
        }

        if (code.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Código do exame deve ter no máximo 50 caracteres");
        }
    }

    private static void validateNameAndSampleType(
            String name,
            SampleType sampleType) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome do exame deve ser informado");
        }

        if (name.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Nome do exame deve ter no máximo 150 caracteres");
        }

        if (sampleType == null) {
            throw new IllegalArgumentException(
                    "Tipo de amostra deve ser informado");
        }
    }

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
