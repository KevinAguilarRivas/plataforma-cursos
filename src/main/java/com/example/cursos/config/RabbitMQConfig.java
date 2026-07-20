package com.example.cursos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombres de colas, exchange y routing keys
    public static final String EXCHANGE = "cursos.exchange";

    public static final String COLA_INSCRIPCIONES = "cola.inscripciones";
    public static final String ROUTING_INSCRIPCIONES = "inscripciones.routingkey";

    public static final String COLA_INSCRIPCIONES_ERROR = "cola.inscripciones.error";
    public static final String ROUTING_INSCRIPCIONES_ERROR = "inscripciones.error.routingkey";

    @Bean
    public DirectExchange cursosExchange() {
        return new DirectExchange(EXCHANGE);
    }

    // Cola 1: flujo normal de inscripciones a cursos
    @Bean
    public Queue colaInscripciones() {
        return new Queue(COLA_INSCRIPCIONES, true); // durable
    }

    // Cola 2: inscripciones que fallaron al publicarse/procesarse
    @Bean
    public Queue colaInscripcionesError() {
        return new Queue(COLA_INSCRIPCIONES_ERROR, true); // durable
    }

    @Bean
    public Binding bindingColaInscripciones(Queue colaInscripciones, DirectExchange cursosExchange) {
        return BindingBuilder.bind(colaInscripciones).to(cursosExchange).with(ROUTING_INSCRIPCIONES);
    }

    @Bean
    public Binding bindingColaInscripcionesError(Queue colaInscripcionesError, DirectExchange cursosExchange) {
        return BindingBuilder.bind(colaInscripcionesError).to(cursosExchange).with(ROUTING_INSCRIPCIONES_ERROR);
    }

    // Serializa/deserializa los mensajes como JSON (en vez de Java serializado)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
