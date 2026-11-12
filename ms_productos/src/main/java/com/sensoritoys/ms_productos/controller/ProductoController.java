package com.sensoritoys.ms_productos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sensoritoys.ms_productos.dto.StockDashboardDTO;
import com.sensoritoys.ms_productos.model.Producto;
import com.sensoritoys.ms_productos.service.ProductoService;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto nuevo = productoService.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, producto));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Producto> descontarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        Producto actualizado = productoService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        productoService.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard/stock")
    public ResponseEntity<List<StockDashboardDTO>> obtenerDashboardStock() {
        return ResponseEntity.ok(productoService.obtenerDashboardStock());
    }
}
