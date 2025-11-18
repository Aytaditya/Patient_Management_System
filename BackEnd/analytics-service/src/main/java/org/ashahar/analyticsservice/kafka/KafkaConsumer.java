package org.ashahar.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consume(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            // ... Perform any business related to analytics

            log.info("Received PatientEvent: ID={}, Name={}, Email={}, EventType={}",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail(),
                    patientEvent.getEventType());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing PatientEvent from Kafka: {}", e.getMessage());
        }
    }
}
