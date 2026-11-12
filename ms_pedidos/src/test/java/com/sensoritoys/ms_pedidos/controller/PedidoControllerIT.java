package com.sensoritoys.ms_pedidos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.sensoritoys.ms_pedidos.repository.PedidoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerIT {

    private static final String PEDIDO_JSON = """
            {
              "usuarioId": 1,
              "detalles": [
                { "productoId": 2, "cantidad": 3, "precioUnitario": 10.00 }
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
    }

    @Test
    void testCrearYListarPedidos() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fechaCreacion").exists())
                .andExpect(jsonPath("$.total").value(30.00));

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(30.00));
    }

    @Test
    void testObtenerYEliminarPedido() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_JSON))
                .andExpect(status().isCreated());

        Long id = pedidoRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/pedidos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1));

        mockMvc.perform(delete("/api/v1/pedidos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pedidos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarPedidoInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/api/v1/pedidos/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearPedidoSinDetallesRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\": 1, \"detalles\": []}"))
                .andExpect(status().isBadRequest());
    }
}
