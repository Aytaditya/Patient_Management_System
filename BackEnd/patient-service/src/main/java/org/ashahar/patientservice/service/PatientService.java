package org.ashahar.patientservice.service;

import org.ashahar.patientservice.dto.PatientRequestDTO;
import org.ashahar.patientservice.dto.PatientResponseDTO;
import org.ashahar.patientservice.exception.EmailAlreadyExistsException;
import org.ashahar.patientservice.exception.PatientNotFoundException;
import org.ashahar.patientservice.grpc.BillingServiceGrpcClient;
import org.ashahar.patientservice.kafka.kafkaProducer;
import org.ashahar.patientservice.mapper.PatientMapper;
import org.ashahar.patientservice.model.Patient;
import org.ashahar.patientservice.repository.PatientRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepo patientRepo;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final org.ashahar.patientservice.kafka.kafkaProducer kafkaProducer;

    public PatientService(PatientRepo patientRepo, BillingServiceGrpcClient billingServiceGrpcClient, kafkaProducer kafkaProducer) {
        this.patientRepo = patientRepo;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepo.findAll();

        return patients.stream()
                .map(PatientMapper::toDTO)
                .toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepo.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepo.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

        if(patientRepo.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("Email already exists: " + patientRequestDTO.getEmail());
        }
        
        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepo.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        patientRepo.deleteById(id);
    }
}
