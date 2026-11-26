package com.sensoritoys.ms_productos.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.persistence.EntityNotFoundException;

class ImagenServiceTest {

    @TempDir
    Path directorio;

    @Test
    void guardaImagenValidaYPermiteCargarla() {
        ImagenService servicio = new ImagenService(directorio.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.png", "image/png", new byte[] {1, 2, 3});

        String nombre = servicio.guardar(archivo);

        assertTrue(nombre.endsWith(".png"));
        assertTrue(servicio.cargar(nombre).exists());
    }

    @Test
    void rechazaTiposQueNoSonImagen() {
        ImagenService servicio = new ImagenService(directorio.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "script.html", "text/html", new byte[] {1});

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(archivo));
    }

    @Test
    void rechazaNombresQueIntentanSalirDeLaCarpeta() {
        ImagenService servicio = new ImagenService(directorio.toString());

        assertThrows(EntityNotFoundException.class, () -> servicio.cargar("../application.properties"));
    }
}
