package com.sensoritoys.ms_productos.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sensoritoys.ms_productos.exception.OperacionNoPermitidaException;
import com.sensoritoys.ms_productos.model.Categoria;
import com.sensoritoys.ms_productos.repository.CategoriaRepository;
import com.sensoritoys.ms_productos.repository.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria guardarCategoria(Categoria categoria) {
        validarNombre(categoria);
        if (categoriaRepository.findByNombreIgnoreCase(categoria.getNombre().trim()).isPresent()) {
            throw new OperacionNoPermitidaException("Ya existe una categoría con ese nombre");
        }
        categoria.setId(null);
        categoria.setNombre(categoria.getNombre().trim());
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizarCategoria(Long id, Categoria datos) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + id));
        validarNombre(datos);

        String nombre = datos.getNombre().trim();
        boolean nombreEnUso = categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .isPresent();
        if (nombreEnUso) {
            throw new OperacionNoPermitidaException("Ya existe una categoría con ese nombre");
        }

        existente.setNombre(nombre);
        existente.setDescripcion(datos.getDescripcion());
        return categoriaRepository.save(existente);
    }

    public void eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoría no encontrada con ID: " + id);
        }
        if (productoRepository.existsByCategoriaId(id)) {
            throw new OperacionNoPermitidaException("No se puede eliminar una categoría con productos asociados");
        }
        categoriaRepository.deleteById(id);
    }

    private void validarNombre(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
    }
}
