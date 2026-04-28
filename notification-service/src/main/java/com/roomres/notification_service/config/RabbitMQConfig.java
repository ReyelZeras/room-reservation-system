package com.roomres.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Constantes Reservas
    public static final String EXCHANGE_RESERVA = "booking.v1.events";
    public static final String QUEUE_RESERVA_CRIADA = "booking.reservation-created.notification";
    public static final String ROUTING_KEY_RESERVA_CRIADA = "reservation.created";

    // Constantes Usuários (Email Opt-in e Senha)
    public static final String EXCHANGE_USER = "user.v1.events";
    public static final String QUEUE_USER_REGISTERED = "notification.user-registered";
    public static final String ROUTING_KEY_REGISTERED = "user.registered";

    // NOVA FILA E CHAVE PARA RESET DE SENHA
    public static final String QUEUE_PASSWORD_RESET = "notification.password-reset";
    public static final String ROUTING_KEY_PASSWORD_RESET = "user.password.reset";

    @Bean public TopicExchange reservaExchange() { return new TopicExchange(EXCHANGE_RESERVA); }
    @Bean public Queue reservaQueue() { return new Queue(QUEUE_RESERVA_CRIADA, true); }
    @Bean public Binding reservaBinding() { return BindingBuilder.bind(reservaQueue()).to(reservaExchange()).with(ROUTING_KEY_RESERVA_CRIADA); }

    // Beans do Opt-in
    @Bean public TopicExchange userExchange() { return new TopicExchange(EXCHANGE_USER); }
    @Bean public Queue userRegisteredQueue() { return new Queue(QUEUE_USER_REGISTERED, true); }
    @Bean public Binding userBinding() { return BindingBuilder.bind(userRegisteredQueue()).to(userExchange()).with(ROUTING_KEY_REGISTERED); }

    // NOVOS BEANS DO RESET DE SENHA
    @Bean public Queue passwordResetQueue() { return new Queue(QUEUE_PASSWORD_RESET, true); }
    @Bean public Binding passwordResetBinding() { return BindingBuilder.bind(passwordResetQueue()).to(userExchange()).with(ROUTING_KEY_PASSWORD_RESET); }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }
}