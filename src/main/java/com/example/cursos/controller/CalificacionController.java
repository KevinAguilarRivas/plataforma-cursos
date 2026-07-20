package com.example.cursos.controller;

import com.example.cursos.model.Calificacion;
import com.example.cursos.repository.CalificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionRepository calificacionRepository;

    /**
     * POST /calificaciones/registrar
     * El instructor registra la nota de un estudiante en un examen del curso.
     * Se persiste directamente en Oracle Cloud para reflejarse en tiempo real.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Calificacion> registrar(@RequestBody Calificacion calificacion) {
        calificacion.setFechaRegistro(LocalDateTime.now());
        Calificacion guardada = calificacionRepository.save(calificacion);
        return ResponseEntity.ok(guardada);
    }

    /**
     * GET /calificaciones/consultar
     * Consulta las calificaciones de un curso y/o un estudiante.
     */
    @GetMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultar(
            @RequestParam(required = false) String codigoCurso,
            @RequestParam(required = false) String rutEstudiante) {
        List<Calificacion> resultado;
        if (codigoCurso != null && !codigoCurso.isEmpty()) {
            resultado = calificacionRepository.findByCodigoCurso(codigoCurso);
        } else if (rutEstudiante != null && !rutEstudiante.isEmpty()) {
            resultado = calificacionRepository.findByRutEstudiante(rutEstudiante);
        } else {
            resultado = calificacionRepository.findAll();
        }
        return ResponseEntity.ok(Map.of(
                "total", resultado.size(),
                "calificaciones", resultado));
    }
}
