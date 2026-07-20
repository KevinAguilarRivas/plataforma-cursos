package com.example.cursos.producer;

import com.example.cursos.config.RabbitMQConfig;
import com.example.cursos.model.Inscripcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente unico encargado de transmitir mensajes hacia ambas colas:
 * - Intenta publicar la inscripcion en la cola 1 (cola.inscripciones).
 * - Si falla, reenvia el mismo mensaje a la cola 2 (cola.inscripciones.error).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InscripcionProducer {

    private final RabbitTemplate rabbitTemplate;

    public void enviarInscripcion(Inscripcion inscripcion) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_INSCRIPCIONES,
                    inscripcion);
            log.info("Inscripcion {} enviada a cola.inscripciones", inscripcion.getNumeroInscripcion());
        } catch (AmqpException e) {
            log.error("Fallo al enviar inscripcion {} a cola.inscripciones, se envia a cola de errores. Motivo: {}",
                    inscripcion.getNumeroInscripcion(), e.getMessage());
            enviarAColaError(inscripcion, e.getMessage());
        }
    }

    private void enviarAColaError(Inscripcion inscripcion, String motivoError) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_INSCRIPCIONES_ERROR,
                    inscripcion);
            log.info("Inscripcion {} enviada a cola.inscripciones.error (motivo original: {})",
                    inscripcion.getNumeroInscripcion(), motivoError);
        } catch (AmqpException ex) {
            // Ultimo recurso: si tampoco se puede publicar en la cola de errores, solo se deja registro en el log.
            log.error("No se pudo enviar la inscripcion {} ni siquiera a la cola de errores: {}",
                    inscripcion.getNumeroInscripcion(), ex.getMessage());
        }
    }
}
