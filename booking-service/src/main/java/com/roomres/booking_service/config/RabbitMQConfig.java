package com.roomres.booking_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Constantes que o Publisher vai usar (Nomes padronizados para evitar o erro de 'Cannot resolve symbol')
    public static final String EXCHANGE_RESERVA = "booking.v1.events";
    public static final String QUEUE_RESERVA_CRIADA = "booking.reservation-created.notification";
    public static final String ROUTING_KEY_RESERVA_CRIADA = "reservation.created";

    @Bean
    public TopicExchange reservaExchange() {
        return new TopicExchange(EXCHANGE_RESERVA);
    }

    @Bean
    public Queue reservaCriadaQueue() {
        return QueueBuilder.durable(QUEUE_RESERVA_CRIADA).build();
    }

    @Bean
    public Binding bindingReservaCriada() {
        return BindingBuilder.bind(reservaCriadaQueue())
                .to(reservaExchange())
                .with(ROUTING_KEY_RESERVA_CRIADA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Necessário para enviar LocalDateTime sem erro
        return new Jackson2JsonMessageConverter(mapper);
    }
}