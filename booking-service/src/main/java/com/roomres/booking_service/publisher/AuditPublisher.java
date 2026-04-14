package com.roomres.booking_service.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "booking-audit-events";

    public void sendAuditEvent(String acao, String detalhes) {
        log.info("Publicando log de auditoria no Kafka: {}", acao);
        String mensagem = String.format("Acao: %s | Detalhes: %s", acao, detalhes);

        try {
            kafkaTemplate.send(TOPIC, mensagem);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para o Kafka: {}", e.getMessage());
        }
    }
}