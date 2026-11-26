package com.sensoritoys.ms_productos.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ImagenService {

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|png|webp)$");
    private static final Map<String, String> EXTENSION_POR_TIPO = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final Path directorio;

    public ImagenService(@Value("${sensoritoys.uploads.directorio:uploads/productos}") String directorio) {
        this.directorio = Paths.get(directorio).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una imagen");
        }
        String extension = EXTENSION_POR_TIPO.get(archivo.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WEBP");
        }

        String nombre = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(directorio);
            archivo.transferTo(directorio.resolve(nombre));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
        return nombre;
    }

    public Resource cargar(String nombre) {
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new EntityNotFoundException("Imagen no encontrada");
        }
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio) || !Files.isRegularFile(ruta)) {
            throw new EntityNotFoundException("Imagen no encontrada");
        }
        return new PathResource(ruta);
    }

    public String tipoContenido(String nombre) {
        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
