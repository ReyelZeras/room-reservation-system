package com.roomres.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_RESERVA_CRIADA = "booking.reservation-created.notification";
    public static final String EXCHANGE_RESERVA = "booking.v1.events";
    public static final String ROUTING_KEY_RESERVA_CRIADA = "reservation.created";

    // 1. Garante que a Fila será criada
    @Bean
    public Queue reservaQueue() {
        return new Queue(QUEUE_RESERVA_CRIADA, true); // true = durable (sobrevive se o RabbitMQ reiniciar)
    }

    // 2. Garante que o Exchange será criado
    @Bean
    public TopicExchange reservaExchange() {
        return new TopicExchange(EXCHANGE_RESERVA);
    }

    // 3. Liga a Fila ao Exchange
    @Bean
    public Binding reservaBinding(Queue reservaQueue, TopicExchange reservaExchange) {
        return BindingBuilder.bind(reservaQueue).to(reservaExchange).with(ROUTING_KEY_RESERVA_CRIADA);
    }

    // Conversor de JSON (Mantido como você já tinha)
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);

        // Permite a desserialização de pacotes externos
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }
}