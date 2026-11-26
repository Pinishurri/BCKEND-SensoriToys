package com.sensoritoys.ms_productos.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ImagenControllerIT {

    private static final String RUTA_SUBIDA = "/api/v1/productos/imagen";

    @TempDir
    static Path directorio;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("sensoritoys.uploads.directorio", () -> directorio.toString());
    }

    @Test
    void subeImagenValidaYLaEntregaPorSuUrl() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.png", "image/png", new byte[] {1, 2, 3});

        String respuesta = mockMvc.perform(multipart(RUTA_SUBIDA).file(archivo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(startsWith("/api/v1/productos/imagenes/")))
                .andReturn().getResponse().getContentAsString();

        String url = respuesta.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(new byte[] {1, 2, 3}));
    }

    @Test
    void rechazaArchivosQueNoSonImagen() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "pagina.html", "text/html", new byte[] {1});

        mockMvc.perform(multipart(RUTA_SUBIDA).file(archivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La imagen debe ser JPG, PNG o WEBP"));
    }

    @Test
    void rechazaSolicitudSinArchivo() throws Exception {
        mockMvc.perform(multipart(RUTA_SUBIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Selecciona una imagen"));
    }

    @Test
    void respondeNoEncontradoParaImagenInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/productos/imagenes/00000000-0000-0000-0000-000000000000.png"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Imagen no encontrada"));
    }
}
