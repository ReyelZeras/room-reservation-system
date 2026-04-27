package com.roomres.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_USER = "user.v1.events";

    // Cria a Exchange (Trocador) de onde as mensagens de utilizador vão partir
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE_USER);
    }

    // Fundamental: Converte o Map<String, String> do Java para JSON válido
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }
}