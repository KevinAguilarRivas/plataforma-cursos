package com.example.cursos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCurso {
    private String codigoCurso;
    private String nombreCurso;
    private String instructor;
    private String fecha; // formato: yyyyMMdd ej: 20260319
    private String tituloMaterial;
    private String descripcion;
    private String tipoContenido; // PDF, VIDEO, PRESENTACION
    private String estado; // BORRADOR, PUBLICADO
}
