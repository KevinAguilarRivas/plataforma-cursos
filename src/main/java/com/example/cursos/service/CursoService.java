package com.example.cursos.service;

import com.example.cursos.model.MaterialCurso;
import java.util.List;

public interface CursoService {
    // Crea el material en EFS y retorna la ruta local del archivo
    String crearMaterial(MaterialCurso material);

    // Sube el material desde EFS a S3 organizado por fecha/curso
    String subirMaterialS3(String tituloMaterial, String codigoCurso, String fecha);

    // Descarga el material desde S3
    byte[] descargarMaterial(String codigoCurso, String fecha, String tituloMaterial);

    // Actualiza el contenido de un material en S3 (y en EFS)
    String actualizarMaterial(String codigoCurso, String fecha, String tituloMaterial, MaterialCurso materialActualizado);

    // Elimina un material de S3 (y del EFS si existe)
    void eliminarMaterial(String codigoCurso, String fecha, String tituloMaterial);

    // Consulta materiales por curso y/o fecha
    List<String> consultarMateriales(String codigoCurso, String fecha);
}
