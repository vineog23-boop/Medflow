package br.com.medflow.patient.web;

import br.com.medflow.patient.application.PatientService;
import br.com.medflow.patient.application.dto.PatientDtoRequest;
import br.com.medflow.patient.application.dto.PatientDtoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<Page<PatientDtoResponse>> findAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<PatientDtoResponse> response = patientService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDtoResponse> findById(@PathVariable UUID id) {
        PatientDtoResponse response = patientService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PatientDtoResponse> create(
            @Valid @RequestBody PatientDtoRequest request) {

        PatientDtoResponse response = patientService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDtoResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PatientDtoRequest request) {

        PatientDtoResponse response = patientService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
