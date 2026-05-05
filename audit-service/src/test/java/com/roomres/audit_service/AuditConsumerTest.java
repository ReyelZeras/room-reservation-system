package com.roomres.audit_service;

import com.roomres.audit_service.consumer.AuditConsumer;
import com.roomres.audit_service.model.AuditLog;
import com.roomres.audit_service.repository.AuditRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditConsumerTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditConsumer auditConsumer;

    @Test
    @DisplayName("Deve consumir mensagem do Kafka, construir a entidade e salvar no banco")
    void consume_Success() {
        // Arrange
        String kafkaMessage = "Acao: RESERVA_CRIADA | Detalhes: Reserva ID: d290f1ee-6c54-4b01-90e6-d701748f0851";

        // Act
        auditConsumer.consume(kafkaMessage);

        // Assert
        // Usamos o ArgumentCaptor para capturar o objeto exato que o método consume() criou internamente
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);

        // Verifica se o repository.save() foi chamado exatamente 1 vez
        verify(auditRepository, times(1)).save(logCaptor.capture());

        // Extrai a entidade capturada e valida se os dados foram preenchidos corretamente
        AuditLog savedLog = logCaptor.getValue();
        assertNotNull(savedLog);
        assertEquals(kafkaMessage, savedLog.getEventMessage());
        assertNotNull(savedLog.getTimestamp());
    }
}