package com.example.cursos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tabla en Oracle Cloud donde se persisten las inscripciones consumidas
 * desde la cola 1 (cola.inscripciones).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INSCRIPCIONES_CURSO_COLA")
public class InscripcionColaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NUMERO_INSCRIPCION", nullable = false)
    private String numeroInscripcion;

    @Column(name = "RUT_ESTUDIANTE", nullable = false)
    private String rutEstudiante;

    @Column(name = "NOMBRE_ESTUDIANTE")
    private String nombreEstudiante;

    @Column(name = "CODIGO_CURSO", nullable = false)
    private String codigoCurso;

    @Column(name = "NOMBRE_CURSO")
    private String nombreCurso;

    @Column(name = "FECHA", nullable = false)
    private String fecha;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "FECHA_PROCESAMIENTO")
    private LocalDateTime fechaProcesamiento;
}
