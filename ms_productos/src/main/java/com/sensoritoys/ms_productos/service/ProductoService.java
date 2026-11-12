package com.sensoritoys.ms_productos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sensoritoys.ms_productos.dto.StockDashboardDTO;
import com.sensoritoys.ms_productos.model.Categoria;
import com.sensoritoys.ms_productos.model.Producto;
import com.sensoritoys.ms_productos.repository.CategoriaRepository;
import com.sensoritoys.ms_productos.repository.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {

    private static final int UMBRAL_STOCK_BAJO = 5;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findByIdAndActivoTrue(id);
    }

    public Producto guardarProducto(Producto producto) {
        validar(producto);
        producto.setId(null);
        producto.setCategoria(resolverCategoria(producto.getCategoria()));
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto datos) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        validar(datos);

        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setCategoria(resolverCategoria(datos.getCategoria()));
        existente.setEdad(datos.getEdad());
        existente.setImagenUrl(datos.getImagenUrl());
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return productoRepository.save(existente);
    }

    public Producto actualizarStock(Long id, Integer cantidadARestar) {
        if (cantidadARestar == null || cantidadARestar <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        if (producto.getStock() < cantidadARestar) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        producto.setStock(producto.getStock() - cantidadARestar);
        return productoRepository.save(producto);
    }

    public void desactivarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    public List<StockDashboardDTO> obtenerDashboardStock() {
        return productoRepository.findAll().stream()
                .map(producto -> new StockDashboardDTO(
                        producto.getId(),
                        producto.getNombre(),
                        producto.getStock(),
                        producto.getStock() <= UMBRAL_STOCK_BAJO))
                .toList();
    }

    private void validar(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    private Categoria resolverCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            return null;
        }
        return categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + categoria.getId()));
    }
}
