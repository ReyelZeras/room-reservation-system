package com.roomres.user_service.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String EXCHANGE = "user.v1.events";

    public static final String ROUTING_KEY_REGISTERED = "user.registered";
    public static final String ROUTING_KEY_PASSWORD_RESET = "user.password.reset"; // NOVA CHAVE

    public void sendVerificationEmailEvent(String email, String name, String token) {
        log.info("Enviando evento de registro (Ativação) para o RabbitMQ...");
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("name", name);
        payload.put("token", token);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_REGISTERED, payload);
    }

    // NOVO DISPARO DE MENSAGEM
    public void sendPasswordResetEvent(String email, String name, String token) {
        log.info("Enviando evento de Recuperação de Senha para o RabbitMQ...");
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("name", name);
        payload.put("token", token);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_PASSWORD_RESET, payload);
    }
}