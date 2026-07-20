package com.example.cursos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {
    private String numeroInscripcion;
    private String rutEstudiante;
    private String nombreEstudiante;
    private String codigoCurso;
    private String nombreCurso;
    private String fecha; // formato: yyyyMMdd ej: 20260319
    private String estado; // PENDIENTE, CONFIRMADA, RECHAZADA
}
