package com.example.cursos.service;

import com.example.cursos.config.RabbitMQConfig;
import com.example.cursos.model.Inscripcion;
import com.example.cursos.model.InscripcionColaEntity;
import com.example.cursos.repository.InscripcionColaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio invocado por el endpoint adicional (POST /inscripciones/procesar-cola).
 * Consume (drena) los mensajes pendientes en la cola 1 (cola.inscripciones) y los
 * persiste en la base de datos Oracle Cloud, en la tabla INSCRIPCIONES_CURSO_COLA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InscripcionColaConsumerService {

    private final RabbitTemplate rabbitTemplate;
    private final InscripcionColaRepository repository;

    public List<InscripcionColaEntity> procesarMensajesPendientes() {
        List<InscripcionColaEntity> procesadas = new ArrayList<>();

        Object mensaje;
        // receiveAndConvert(queue) hace polling no bloqueante: retorna null cuando la cola queda vacia
        while ((mensaje = rabbitTemplate.receiveAndConvert(RabbitMQConfig.COLA_INSCRIPCIONES)) != null) {
            if (!(mensaje instanceof Inscripcion inscripcion)) {
                log.warn("Mensaje descartado: no corresponde a una Inscripcion ({})", mensaje);
                continue;
            }

            InscripcionColaEntity entity = InscripcionColaEntity.builder()
                    .numeroInscripcion(inscripcion.getNumeroInscripcion())
                    .rutEstudiante(inscripcion.getRutEstudiante())
                    .nombreEstudiante(inscripcion.getNombreEstudiante())
                    .codigoCurso(inscripcion.getCodigoCurso())
                    .nombreCurso(inscripcion.getNombreCurso())
                    .fecha(inscripcion.getFecha())
                    .estado(inscripcion.getEstado())
                    .fechaProcesamiento(LocalDateTime.now())
                    .build();

            repository.save(entity);
            procesadas.add(entity);
            log.info("Inscripcion {} consumida de cola.inscripciones y guardada en Oracle (tabla INSCRIPCIONES_CURSO_COLA)",
                    inscripcion.getNumeroInscripcion());
        }

        return procesadas;
    }
}
