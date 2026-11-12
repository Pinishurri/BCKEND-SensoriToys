package com.sensoritoys.ms_usuarios.controller;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensoritoys.ms_usuarios.dto.LoginRequestDTO;
import com.sensoritoys.ms_usuarios.model.EstadoUsuario;
import com.sensoritoys.ms_usuarios.model.NombreRol;
import com.sensoritoys.ms_usuarios.model.Rol;
import com.sensoritoys.ms_usuarios.model.Usuario;
import com.sensoritoys.ms_usuarios.repository.RolRepository;
import com.sensoritoys.ms_usuarios.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Rol rolAdmin;
    private Rol rolCliente;

    @BeforeEach
    void cleanDb() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        rolAdmin = rolRepository.save(new Rol(null, NombreRol.ADMIN));
        rolCliente = rolRepository.save(new Rol(null, NombreRol.CLIENTE));
    }

    private Usuario guardarUsuario(String nombre, String email, String contrasenia, Rol rol) {
        return usuarioRepository.save(new Usuario(null, nombre, email, passwordEncoder.encode(contrasenia),
                "Calle 1", "ref-001", EstadoUsuario.ACTIVO, null, rol));
    }

    @Test
    void testRegistroAsignaClienteYNoExponeContrasenia() throws Exception {
        Map<String, Object> cuerpo = Map.of(
                "nombre", "Ana Lopez",
                "email", "ana@test.com",
                "contrasenia", "clave123",
                "direccionEnvio", "Calle 1",
                "referenciaPago", "ref-001");

        mockMvc.perform(post("/api/v1/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuerpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Ana Lopez"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.contraseniaHash").doesNotExist());
    }

    @Test
    void testRegistroIgnoraRolEnviadoPorElCliente() throws Exception {
        Map<String, Object> cuerpo = Map.of(
                "nombre", "Intruso",
                "email", "intruso@test.com",
                "contrasenia", "clave123",
                "rol", Map.of("id", rolAdmin.getId()));

        mockMvc.perform(post("/api/v1/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuerpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void testRegistroConCorreoDuplicadoDevuelveConflicto() throws Exception {
        guardarUsuario("Ana Lopez", "ana@test.com", "clave123", rolCliente);
        Map<String, Object> cuerpo = Map.of("nombre", "Otra Ana", "email", "ana@test.com", "contrasenia", "clave456");

        mockMvc.perform(post("/api/v1/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuerpo)))
                .andExpect(status().isConflict());
    }

    @Test
    void testEliminarUsuarioLogicamente() throws Exception {
        Usuario guardado = guardarUsuario("Pedro Soto", "pedro@test.com", "clave456", rolCliente);

        mockMvc.perform(delete("/api/v1/usuarios/" + guardado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/usuarios/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void testIniciarSesionDevuelveTokenYRol() throws Exception {
        guardarUsuario("Ana Lopez", "ana@test.com", "clave123", rolCliente);
        LoginRequestDTO login = new LoginRequestDTO("ana@test.com", "clave123");

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    void testIniciarSesionConContraseniaIncorrectaDevuelve401() throws Exception {
        guardarUsuario("Ana Lopez", "ana@test.com", "clave123", rolCliente);
        LoginRequestDTO login = new LoginRequestDTO("ana@test.com", "incorrecta");

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
