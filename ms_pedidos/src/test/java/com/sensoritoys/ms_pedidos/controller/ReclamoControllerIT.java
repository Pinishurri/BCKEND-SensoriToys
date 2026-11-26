package com.sensoritoys.ms_pedidos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.sensoritoys.ms_pedidos.repository.ReclamoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ReclamoControllerIT {

    private static final String RECLAMO_JSON = """
            {
              "usuarioId": 1,
              "motivo": "RETRASO",
              "descripcion": "El pedido no ha llegado todavía"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReclamoRepository reclamoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        reclamoRepository.deleteAll();
        pedidoRepository.deleteAll();
    }

    @Test
    void testCrearYListarReclamos() throws Exception {
        mockMvc.perform(post("/api/v1/reclamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RECLAMO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.fechaCreacion").exists());

        mockMvc.perform(get("/api/v1/reclamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motivo").value("RETRASO"));

        mockMvc.perform(get("/api/v1/reclamos/usuario/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(1));

        mockMvc.perform(get("/api/v1/reclamos/usuario/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testDescripcionCortaRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/reclamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "usuarioId": 1, "motivo": "OTRO", "descripcion": "corto" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testMotivoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/reclamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "usuarioId": 1, "motivo": "INEXISTENTE", "descripcion": "Descripción suficiente" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPedidoInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/v1/reclamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "usuarioId": 1, "pedidoId": 9999, "motivo": "REEMBOLSO", "descripcion": "Quiero mi reembolso" }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarEstado() throws Exception {
        mockMvc.perform(post("/api/v1/reclamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RECLAMO_JSON))
                .andExpect(status().isCreated());

        Long id = reclamoRepository.findAll().get(0).getId();

        mockMvc.perform(patch("/api/v1/reclamos/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_REVISION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
    }
}
