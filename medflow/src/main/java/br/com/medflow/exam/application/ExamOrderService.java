package br.com.medflow.exam.application;

import br.com.medflow.exam.application.dto.CreateExamRequestDto;
import br.com.medflow.exam.application.dto.UpdateExamRequestDto;
import br.com.medflow.exam.domain.ExamOrder;
import br.com.medflow.exam.persistence.ExamOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExamOrderService {

    private final ExamOrderRepository examOrderRepository;

    public ExamOrderService(ExamOrderRepository examOrderRepository) {
        this.examOrderRepository = examOrderRepository;
    }

    public ExamOrder create(CreateExamRequestDto request) {
        ExamOrder examOrder = new ExamOrder(
                request.patientId(),
                request.examCode(),
                request.priority()
        );

        ExamOrder savedExamOrder = examOrderRepository.save(examOrder);
        return savedExamOrder;
    }

    public ExamOrder findById(UUID id) {
        return examOrderRepository.findById(id)
                .orElseThrow(() -> new ExamOrderNotFoundException(id));
    }

    public Page<ExamOrder> findAll(Pageable pageable) {
        Page<ExamOrder> examOrders = examOrderRepository.findAll(pageable);
        return examOrders;
    }

    public ExamOrder update(UUID id, UpdateExamRequestDto request) {
        ExamOrder examOrder = findById(id);

        examOrder.update(
                request.patientId(),
                request.examCode(),
                request.priority()
        );

        return examOrderRepository.save(examOrder);
    }

    public void delete(UUID id) {
        ExamOrder examOrder = findById(id);
        examOrderRepository.delete(examOrder);
    }
}
