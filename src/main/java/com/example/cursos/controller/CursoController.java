package com.example.cursos.controller;

import com.example.cursos.model.MaterialCurso;
import com.example.cursos.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    /**
     * POST /cursos/crear
     * El instructor crea un material de curso y lo guarda en EFS.
     */
    @PostMapping("/crear")
    public ResponseEntity<Map<String, String>> crearMaterial(@RequestBody MaterialCurso material) {
        String rutaEfs = cursoService.crearMaterial(material);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Material creado exitosamente en EFS",
                "rutaEfs", rutaEfs,
                "tituloMaterial", material.getTituloMaterial()));
    }

    /**
     * POST /cursos/subir
     * Sube el material desde EFS a S3 (almacenamiento Cloud).
     */
    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirMaterial(
            @RequestParam String tituloMaterial,
            @RequestParam String codigoCurso,
            @RequestParam String fecha) {
        String keyS3 = cursoService.subirMaterialS3(tituloMaterial, codigoCurso, fecha);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Material subido exitosamente a S3",
                "keyS3", keyS3));
    }

    /**
     * GET /cursos/descargar
     * El estudiante o instructor descarga el material desde S3.
     */
    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargarMaterial(
            @RequestParam String codigoCurso,
            @RequestParam String fecha,
            @RequestParam String tituloMaterial) {
        byte[] contenido = cursoService.descargarMaterial(codigoCurso, fecha, tituloMaterial);
        String nombreArchivo = tituloMaterial + ".txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }

    /**
     * PUT /cursos/actualizar
     * Actualiza un material existente en EFS y S3.
     */
    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, String>> actualizarMaterial(
            @RequestParam String codigoCurso,
            @RequestParam String fecha,
            @RequestParam String tituloMaterial,
            @RequestBody MaterialCurso materialActualizado) {
        String keyS3 = cursoService.actualizarMaterial(codigoCurso, fecha, tituloMaterial, materialActualizado);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Material actualizado exitosamente",
                "keyS3", keyS3));
    }

    /**
     * DELETE /cursos/eliminar
     * Elimina un material de S3 y EFS.
     */
    @DeleteMapping("/eliminar")
    public ResponseEntity<Map<String, String>> eliminarMaterial(
            @RequestParam String codigoCurso,
            @RequestParam String fecha,
            @RequestParam String tituloMaterial) {
        cursoService.eliminarMaterial(codigoCurso, fecha, tituloMaterial);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Material eliminado exitosamente",
                "tituloMaterial", tituloMaterial));
    }

    /**
     * GET /cursos/consultar
     * Consulta materiales por curso y/o fecha (acceso al contenido del curso).
     */
    @GetMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultarMateriales(
            @RequestParam(required = false) String codigoCurso,
            @RequestParam(required = false) String fecha) {
        List<String> materiales = cursoService.consultarMateriales(codigoCurso, fecha);
        return ResponseEntity.ok(Map.of(
                "total", materiales.size(),
                "materiales", materiales));
    }
}
