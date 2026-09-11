package br.com.medflow.exam.persistence;

import br.com.medflow.exam.domain.ExamOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamOrderRepository extends JpaRepository<ExamOrder, UUID> {
}
