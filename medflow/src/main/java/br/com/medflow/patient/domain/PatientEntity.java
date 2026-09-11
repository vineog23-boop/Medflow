package br.com.medflow.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank
    @CPF
    @Column(name = "cpf", nullable = false)
    private String cpf;

    @NotNull
    @PastOrPresent
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected PatientEntity() {
    }

    public PatientEntity(String fullName, String cpf, LocalDate birthDate) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Nome completo deve ser informado");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF deve ser informado");
        }

        if (birthDate == null) {
            throw new IllegalArgumentException("Data de nascimento deve ser informada");
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Data de nascimento não pode estar no futuro");
        }

        this.fullName = fullName.trim();
        this.cpf = cpf.trim();
        this.birthDate = birthDate;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

    }

    public UUID getId() {
        return id;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCpf() {
        return cpf;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }
}
