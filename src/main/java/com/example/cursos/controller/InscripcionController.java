package com.example.cursos.controller;

import com.example.cursos.model.Inscripcion;
import com.example.cursos.model.InscripcionColaEntity;
import com.example.cursos.producer.InscripcionProducer;
import com.example.cursos.service.InscripcionColaConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionProducer inscripcionProducer;
    private final InscripcionColaConsumerService inscripcionColaConsumerService;

    /**
     * POST /inscripciones/inscribir
     * Endpoint PRODUCTOR: el estudiante se inscribe a un curso y el mensaje
     * se publica de forma asincrona en la cola 1 (cola.inscripciones).
     */
    @PostMapping("/inscribir")
    public ResponseEntity<Map<String, String>> inscribir(@RequestBody Inscripcion inscripcion) {
        inscripcionProducer.enviarInscripcion(inscripcion);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Inscripcion enviada a la cola para su procesamiento",
                "numeroInscripcion", inscripcion.getNumeroInscripcion()));
    }

    /**
     * POST /inscripciones/procesar-cola
     * Endpoint CONSUMIDOR: el instructor/administrador consume los mensajes
     * pendientes en la cola 1 (cola.inscripciones) y los persiste en Oracle Cloud.
     */
    @PostMapping("/procesar-cola")
    public ResponseEntity<Map<String, Object>> procesarCola() {
        List<InscripcionColaEntity> procesadas = inscripcionColaConsumerService.procesarMensajesPendientes();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Procesamiento de cola completado",
                "totalProcesadas", procesadas.size(),
                "inscripciones", procesadas));
    }
}
