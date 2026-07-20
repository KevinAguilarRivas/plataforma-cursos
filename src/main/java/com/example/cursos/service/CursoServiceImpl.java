package com.example.cursos.service;

import com.example.cursos.model.MaterialCurso;
import com.example.cursos.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoServiceImpl implements CursoService {

    private final S3Repository s3Repository;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.efs.path}")
    private String efsPath;

    @Override
    public String crearMaterial(MaterialCurso material) {
        try {
            String contenido = buildContenidoMaterial(material);
            String carpeta = efsPath + "/" + material.getFecha() + "/" + material.getCodigoCurso();
            Path dirPath = Paths.get(carpeta);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            String nombreArchivo = material.getTituloMaterial() + ".txt";
            Path archivoPath = dirPath.resolve(nombreArchivo);
            Files.writeString(archivoPath, contenido, StandardCharsets.UTF_8);

            return archivoPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al crear material en EFS: " + e.getMessage(), e);
        }
    }

    @Override
    public String subirMaterialS3(String tituloMaterial, String codigoCurso, String fecha) {
        try {
            String nombreArchivo = tituloMaterial + ".txt";
            Path archivoPath = Paths.get(efsPath, fecha, codigoCurso, nombreArchivo);
            if (!Files.exists(archivoPath)) {
                throw new RuntimeException("El material no existe en EFS: " + archivoPath);
            }
            byte[] contenido = Files.readAllBytes(archivoPath);
            String keyS3 = fecha + "/" + codigoCurso + "/" + nombreArchivo;
            s3Repository.subirArchivo(bucket, keyS3, contenido);
            return keyS3;
        } catch (IOException e) {
            throw new RuntimeException("Error al subir material a S3: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] descargarMaterial(String codigoCurso, String fecha, String tituloMaterial) {
        String key = fecha + "/" + codigoCurso + "/" + tituloMaterial + ".txt";
        return s3Repository.descargarArchivo(bucket, key);
    }

    @Override
    public String actualizarMaterial(String codigoCurso, String fecha, String tituloMaterial, MaterialCurso materialActualizado) {
        try {
            String contenido = buildContenidoMaterial(materialActualizado);
            String nombreArchivo = tituloMaterial + ".txt";
            Path archivoPath = Paths.get(efsPath, fecha, codigoCurso, nombreArchivo);
            if (!Files.exists(archivoPath.getParent())) {
                Files.createDirectories(archivoPath.getParent());
            }
            Files.writeString(archivoPath, contenido, StandardCharsets.UTF_8);
            String keyS3 = fecha + "/" + codigoCurso + "/" + nombreArchivo;
            s3Repository.subirArchivo(bucket, keyS3, contenido.getBytes(StandardCharsets.UTF_8));
            return keyS3;
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar material: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarMaterial(String codigoCurso, String fecha, String tituloMaterial) {
        String nombreArchivo = tituloMaterial + ".txt";
        String keyS3 = fecha + "/" + codigoCurso + "/" + nombreArchivo;
        s3Repository.eliminarArchivo(bucket, keyS3);
        Path archivoPath = Paths.get(efsPath, fecha, codigoCurso, nombreArchivo);
        try {
            Files.deleteIfExists(archivoPath);
        } catch (IOException e) {
            System.err.println("Advertencia: no se pudo borrar del EFS: " + e.getMessage());
        }
    }

    @Override
    public List<String> consultarMateriales(String codigoCurso, String fecha) {
        String prefijo = "";
        if (fecha != null && !fecha.isEmpty() && codigoCurso != null && !codigoCurso.isEmpty()) {
            prefijo = fecha + "/" + codigoCurso + "/";
        } else if (fecha != null && !fecha.isEmpty()) {
            prefijo = fecha + "/";
        } else if (codigoCurso != null && !codigoCurso.isEmpty()) {
            List<String> todos = s3Repository.listarArchivos(bucket, "");
            return todos.stream()
                    .filter(k -> k.contains("/" + codigoCurso + "/"))
                    .toList();
        }
        return s3Repository.listarArchivos(bucket, prefijo);
    }

    private String buildContenidoMaterial(MaterialCurso m) {
        return "========================================\n" +
                "       MATERIAL DE CURSO\n" +
                "========================================\n" +
                "Curso           : " + m.getCodigoCurso() + " - " + m.getNombreCurso() + "\n" +
                "Instructor      : " + m.getInstructor() + "\n" +
                "Fecha           : " + m.getFecha() + "\n" +
                "Título          : " + m.getTituloMaterial() + "\n" +
                "Descripción     : " + m.getDescripcion() + "\n" +
                "Tipo Contenido  : " + m.getTipoContenido() + "\n" +
                "Estado          : " + m.getEstado() + "\n" +
                "========================================\n";
    }
}
