package com.sensoritoys.ms_productos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sensoritoys.ms_productos.dto.StockDashboardDTO;
import com.sensoritoys.ms_productos.model.Categoria;
import com.sensoritoys.ms_productos.model.Producto;
import com.sensoritoys.ms_productos.repository.CategoriaRepository;
import com.sensoritoys.ms_productos.repository.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto crearProducto(Long id, String nombre, double precio, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setDescripcion("Descripción de " + nombre);
        producto.setPrecio(BigDecimal.valueOf(precio));
        producto.setStock(stock);
        return producto;
    }

    @Test
    void testGuardarProducto() {
        Producto producto = crearProducto(null, "Auto Juguete", 15.99, 10);
        Producto guardado = crearProducto(1L, "Auto Juguete", 15.99, 10);
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        Producto resultado = productoService.guardarProducto(producto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertTrue(producto.getActivo());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void testGuardarProductoConPrecioInvalido() {
        Producto producto = crearProducto(null, "Auto Juguete", 0, 10);

        assertThrows(IllegalArgumentException.class, () -> productoService.guardarProducto(producto));
    }

    @Test
    void testGuardarProductoConCategoriaInexistente() {
        Producto producto = crearProducto(null, "Auto Juguete", 15.99, 10);
        producto.setCategoria(new Categoria(99L, null, null));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productoService.guardarProducto(producto));
    }

    @Test
    void testActualizarProductoModificaCamposYMantieneActivoSiNoSeEnvia() {
        Producto existente = crearProducto(1L, "Auto Juguete", 15.99, 10);
        Producto datos = crearProducto(null, "Auto Deportivo", 20.5, 7);
        datos.setActivo(null);
        datos.setEdad("3-6 años");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.actualizarProducto(1L, datos);

        assertEquals("Auto Deportivo", resultado.getNombre());
        assertEquals(7, resultado.getStock());
        assertEquals("3-6 años", resultado.getEdad());
        assertTrue(resultado.getActivo());
    }

    @Test
    void testActualizarProductoNoExistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productoService.actualizarProducto(99L, crearProducto(null, "Auto", 10, 1)));
    }

    @Test
    void testDesactivarProductoNoLoElimina() {
        Producto producto = crearProducto(1L, "Auto Juguete", 15.99, 10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.desactivarProducto(1L);

        assertFalse(producto.getActivo());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void testActualizarStockInsuficiente() {
        Producto producto = crearProducto(1L, "Muñeca", 20.00, 2);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productoService.actualizarStock(1L, 5);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void testActualizarStockConCantidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> productoService.actualizarStock(1L, 0));
    }

    @Test
    void testProductoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            productoService.actualizarStock(99L, 1);
        });
    }

    @Test
    void testDashboardStockMarcaAlertaDeStockBajo() {
        Producto bajo = crearProducto(1L, "Muñeca", 20.00, 2);
        Producto alto = crearProducto(2L, "Auto Juguete", 15.99, 50);
        when(productoRepository.findAll()).thenReturn(List.of(bajo, alto));

        List<StockDashboardDTO> resultado = productoService.obtenerDashboardStock();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getAlertaStockBajo());
        assertEquals(false, resultado.get(1).getAlertaStockBajo());
        assertEquals("Auto Juguete", resultado.get(1).getNombreProducto());
    }
}
