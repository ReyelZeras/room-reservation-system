package com.roomres.audit_service.consumer;

import com.roomres.audit_service.model.AuditLog;
import com.roomres.audit_service.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditConsumer {

    private final AuditRepository auditRepository;

    @KafkaListener(topics = "booking-audit-events", groupId = "audit-group")
    public void consume(String message) {
        log.info("Mensagem de auditoria recebida do Kafka: {}", message);

        AuditLog logEntidade = AuditLog.builder()
                .eventMessage(message)
                .timestamp(LocalDateTime.now())
                .build();

        auditRepository.save(logEntidade);
        log.info("Log de auditoria salvo no banco audit_db");
    }
}