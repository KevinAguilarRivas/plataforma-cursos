package com.example.cursos.repository;

import com.example.cursos.model.InscripcionColaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionColaRepository extends JpaRepository<InscripcionColaEntity, Long> {
}
