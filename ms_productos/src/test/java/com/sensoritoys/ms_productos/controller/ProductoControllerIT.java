package com.sensoritoys.ms_productos.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensoritoys.ms_productos.model.Categoria;
import com.sensoritoys.ms_productos.model.Producto;
import com.sensoritoys.ms_productos.repository.CategoriaRepository;
import com.sensoritoys.ms_productos.repository.ProductoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    private Producto nuevoProducto(String nombre, double precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion("Bloques de construcción");
        producto.setPrecio(BigDecimal.valueOf(precio));
        producto.setStock(stock);
        return producto;
    }

    @Test
    void testCrearYListarProductos() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Táctil", null));
        Producto producto = nuevoProducto("Lego Set", 45.99, 15);
        producto.setCategoria(categoria);
        producto.setEdad("3-6 años");

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Lego Set"))
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Lego Set"))
                .andExpect(jsonPath("$[0].categoria.nombre").value("Táctil"))
                .andExpect(jsonPath("$[0].edad").value("3-6 años"));
    }

    @Test
    void testCrearProductoConPrecioInvalidoDevuelve400() throws Exception {
        Producto producto = nuevoProducto("Lego Set", -1, 15);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testActualizarProducto() throws Exception {
        Producto guardado = productoRepository.save(nuevoProducto("Lego Set", 45.99, 15));
        Producto cambios = nuevoProducto("Lego Set Pro", 50, 20);

        mockMvc.perform(put("/api/v1/productos/" + guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lego Set Pro"))
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    void testEliminarDesactivaProductoYLoOcultaDelCatalogo() throws Exception {
        Producto guardado = productoRepository.save(nuevoProducto("Lego Set", 45.99, 15));

        mockMvc.perform(delete("/api/v1/productos/" + guardado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/v1/productos/" + guardado.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/productos/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    void testCategoriaConProductosNoSePuedeEliminar() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Táctil", null));
        Producto producto = nuevoProducto("Lego Set", 45.99, 15);
        producto.setCategoria(categoria);
        productoRepository.save(producto);

        mockMvc.perform(delete("/api/v1/categorias/" + categoria.getId()))
                .andExpect(status().isConflict());
    }
}
