package br.com.medflow.patient.application;

import br.com.medflow.patient.application.dto.PatientDtoRequest;
import br.com.medflow.patient.application.dto.PatientDtoResponse;
import br.com.medflow.patient.application.exception.PatientNotFoundException;
import br.com.medflow.patient.domain.PatientEntity;
import br.com.medflow.patient.persistence.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientDtoResponse create(PatientDtoRequest request) {
        PatientEntity patient = new PatientEntity(
                request.fullName(),
                request.cpf(),
                request.birthDate()
        );

        PatientEntity savedPatient = patientRepository.save(patient);

        return new PatientDtoResponse(savedPatient);
    }

    public PatientDtoResponse findById(UUID id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return new PatientDtoResponse(patient);
    }

    public Page<PatientDtoResponse> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(PatientDtoResponse::new);
    }

    public PatientDtoResponse update(UUID id, PatientDtoRequest request) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        patient.update(
                request.fullName(),
                request.cpf(),
                request.birthDate()
        );
        PatientEntity updatedPatient = patientRepository.save(patient);

        return new PatientDtoResponse(updatedPatient);
    }

    public void delete(UUID id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        patientRepository.delete(patient);
    }
}
