package com.example.cursos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CALIFICACIONES")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "RUT_ESTUDIANTE", nullable = false)
    private String rutEstudiante;

    @Column(name = "CODIGO_CURSO", nullable = false)
    private String codigoCurso;

    @Column(name = "EXAMEN", nullable = false)
    private String examen;

    @Column(name = "NOTA", nullable = false)
    private double nota;

    @Column(name = "FECHA_REGISTRO")
    private LocalDateTime fechaRegistro;
}
