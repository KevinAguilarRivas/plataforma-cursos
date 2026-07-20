package com.example.cursos.repository;

import com.example.cursos.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByCodigoCurso(String codigoCurso);

    List<Calificacion> findByRutEstudiante(String rutEstudiante);
}
