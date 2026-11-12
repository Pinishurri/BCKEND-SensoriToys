package com.sensoritoys.ms_usuarios.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sensoritoys.ms_usuarios.config.JwtUtil;
import com.sensoritoys.ms_usuarios.dto.LoginResponseDTO;
import com.sensoritoys.ms_usuarios.dto.UsuarioRequestDTO;
import com.sensoritoys.ms_usuarios.exception.CredencialesInvalidasException;
import com.sensoritoys.ms_usuarios.exception.EmailYaRegistradoException;
import com.sensoritoys.ms_usuarios.exception.UsuarioInactivoException;
import com.sensoritoys.ms_usuarios.model.EstadoUsuario;
import com.sensoritoys.ms_usuarios.model.NombreRol;
import com.sensoritoys.ms_usuarios.model.Rol;
import com.sensoritoys.ms_usuarios.model.Usuario;
import com.sensoritoys.ms_usuarios.repository.RolRepository;
import com.sensoritoys.ms_usuarios.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequestDTO solicitud(String email) {
        return new UsuarioRequestDTO("Ana Lopez", email, "clave123", "Calle 1", "ref-001");
    }

    @Test
    void testRegistrarUsuarioAsignaRolClienteYHasheaContrasenia() {
        Rol rolCliente = new Rol(2L, NombreRol.CLIENTE);
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre(NombreRol.CLIENTE)).thenReturn(Optional.of(rolCliente));
        when(passwordEncoder.encode("clave123")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.registrarUsuario(solicitud("  Ana@Test.com "));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertNotNull(resultado);
        assertEquals("ana@test.com", guardado.getEmail());
        assertEquals("hash-bcrypt", guardado.getContraseniaHash());
        assertEquals(EstadoUsuario.ACTIVO, guardado.getEstado());
        assertEquals(NombreRol.CLIENTE, guardado.getRol().getNombre());
    }

    @Test
    void testRegistrarUsuarioDuplicado() {
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(new Usuario()));

        EmailYaRegistradoException exception = assertThrows(EmailYaRegistradoException.class,
                () -> usuarioService.registrarUsuario(solicitud("ana@test.com")));

        assertEquals("El correo electrónico ya está registrado", exception.getMessage());
    }

    @Test
    void testRegistrarUsuarioConEmailInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(solicitud("correo-sin-arroba")));
    }

    @Test
    void testRegistrarUsuarioSinContrasenia() {
        UsuarioRequestDTO datos = new UsuarioRequestDTO("Ana Lopez", "ana@test.com", " ", null, null);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(datos));
    }

    @Test
    void testActualizarUsuarioNoExistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> usuarioService.actualizarUsuario(99L, solicitud("ghost@test.com")));

        assertTrue(exception.getMessage().contains("No existe el usuario"));
    }

    @Test
    void testDesactivarUsuario() {
        Usuario usuario = new Usuario(1L, "Ana Lopez", "ana@test.com", "hash", "Calle 1", "ref-001", EstadoUsuario.ACTIVO, null, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.desactivarUsuario(1L);

        assertEquals(EstadoUsuario.INACTIVO, usuario.getEstado());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testIniciarSesionExitosoIncluyeRolEnToken() {
        Rol rolAdmin = new Rol(1L, NombreRol.ADMIN);
        Usuario usuario = new Usuario(1L, "Ana", "ana@test.com", "hash", "C1", "R1", EstadoUsuario.ACTIVO, LocalDateTime.now(), rolAdmin);
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "hash")).thenReturn(true);
        when(jwtUtil.generarToken("ana@test.com", 1L, "ADMIN")).thenReturn("token-simulado");

        LoginResponseDTO resultado = usuarioService.iniciarSesion("ana@test.com", "clave123");

        assertEquals("token-simulado", resultado.getToken());
        assertEquals("ADMIN", resultado.getRol());
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testIniciarSesionContraseniaIncorrecta() {
        Usuario usuario = new Usuario(1L, "Ana", "ana@test.com", "hash", "C1", "R1", EstadoUsuario.ACTIVO, null, null);
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("malpass", "hash")).thenReturn(false);

        CredencialesInvalidasException exception = assertThrows(CredencialesInvalidasException.class,
                () -> usuarioService.iniciarSesion("ana@test.com", "malpass"));

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void testIniciarSesionUsuarioInactivo() {
        Usuario usuario = new Usuario(1L, "Ana", "ana@test.com", "hash", "C1", "R1", EstadoUsuario.INACTIVO, null, null);
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "hash")).thenReturn(true);

        assertThrows(UsuarioInactivoException.class, () -> usuarioService.iniciarSesion("ana@test.com", "clave123"));
    }
}
