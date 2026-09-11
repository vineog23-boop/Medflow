package br.com.medflow.exam.persistence;

import br.com.medflow.exam.domain.ExamRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRequestRepository extends JpaRepository<ExamRequest, UUID> {
}
