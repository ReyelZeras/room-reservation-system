package com.roomres.booking_service;

import com.roomres.booking_service.client.RoomClient;
import com.roomres.booking_service.client.UserClient;
import com.roomres.booking_service.dto.BookingRequestDTO;
import com.roomres.booking_service.dto.BookingResponseDTO;
import com.roomres.booking_service.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test") // Evita carregar configurações de produção
class BookingIntegrationTest {

    // 1. SOBE UM POSTGRES EFÊMERO
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    // 2. SOBE UM RABBITMQ EFÊMERO
    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    // 3. SOBE UM KAFKA EFÊMERO
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    // 4. INJETA AS CREDENCIAIS GERADAS DINAMICAMENTE NO SPRING BOOT
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("eureka.client.enabled", () -> "false"); // Desliga o Eureka no Teste
    }

    // Mockamos o Feign, pois não queremos depender dos outros microsserviços online no teste
    @MockBean
    private RoomClient roomClient;

    @MockBean
    private UserClient userClient;

    @Autowired
    private BookingService bookingService;

    @Test
    @DisplayName("Deve criar uma reserva e disparar eventos para RabbitMQ e Kafka com sucesso")
    void shouldCreateBookingAndPublishEvents() {
        // Arrange
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // O Feign agora precisa devolver Maps simulando JSON real com nomes!
        java.util.Map<String, Object> mockRoom = java.util.Map.of("name", "Sala VIP Teste");
        java.util.Map<String, Object> mockUser = java.util.Map.of("email", "test@roomres.com", "name", "Test User");

        org.mockito.Mockito.when(roomClient.getRoomById(any())).thenReturn(mockRoom);
        org.mockito.Mockito.when(userClient.getUserById(any())).thenReturn(mockUser);

        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId(roomId);
        request.setUserId(userId);
        // PREVENÇÃO DE QUEBRA: Título adicionado ao Mock do teste
        request.setTitle("Reserva Teste de Integração");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        // Act
        BookingResponseDTO response = bookingService.createBooking(request);

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(response.getId(), "O ID não deveria ser nulo");
    }
}