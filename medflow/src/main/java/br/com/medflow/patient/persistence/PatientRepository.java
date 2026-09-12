package br.com.medflow.patient.persistence;

import br.com.medflow.patient.domain.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {
}
