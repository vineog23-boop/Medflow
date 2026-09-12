package br.com.medflow.exam.definition.persistence;

import br.com.medflow.exam.definition.domain.ExamDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamDefinitionRepository extends JpaRepository<ExamDefinition, UUID> {
}
