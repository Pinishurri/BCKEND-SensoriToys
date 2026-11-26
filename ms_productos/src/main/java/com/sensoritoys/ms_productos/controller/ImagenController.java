package com.sensoritoys.ms_productos.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sensoritoys.ms_productos.service.ImagenService;

@RestController
@RequestMapping("/api/v1/productos")
public class ImagenController {

    private static final String RUTA_IMAGENES = "/api/v1/productos/imagenes/";

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @PostMapping(value = "/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subir(@RequestParam("archivo") MultipartFile archivo) {
        String nombre = imagenService.guardar(archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", RUTA_IMAGENES + nombre));
    }

    @GetMapping("/imagenes/{nombre}")
    public ResponseEntity<Resource> obtener(@PathVariable String nombre) {
        Resource imagen = imagenService.cargar(nombre);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagenService.tipoContenido(nombre)))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(imagen);
    }
}
