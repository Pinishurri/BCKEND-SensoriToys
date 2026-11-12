package com.sensoritoys.ms_usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(UsuarioRequestDTO datos) {
        validarDatos(datos, true);
        String email = normalizarEmail(datos.getEmail());

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new EmailYaRegistradoException("El correo electrónico ya está registrado");
        }

        Rol rolCliente = rolRepository.findByNombre(NombreRol.CLIENTE)
                .orElseThrow(() -> new EntityNotFoundException("No existe el rol CLIENTE"));

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre().trim());
        usuario.setEmail(email);
        usuario.setContraseniaHash(passwordEncoder.encode(datos.getContrasenia()));
        usuario.setDireccionEnvio(datos.getDireccionEnvio());
        usuario.setReferenciaPago(datos.getReferenciaPago());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setRol(rolCliente);
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario actualizarUsuario(Long id, UsuarioRequestDTO datos) {
        validarDatos(datos, false);

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario con ID: " + id));

        String email = normalizarEmail(datos.getEmail());
        if (!existente.getEmail().equals(email) && usuarioRepository.findByEmail(email).isPresent()) {
            throw new EmailYaRegistradoException("El nuevo correo ya está en uso");
        }

        existente.setNombre(datos.getNombre().trim());
        existente.setEmail(email);
        if (datos.getContrasenia() != null && !datos.getContrasenia().isBlank()) {
            existente.setContraseniaHash(passwordEncoder.encode(datos.getContrasenia()));
        }
        existente.setDireccionEnvio(datos.getDireccionEnvio());
        existente.setReferenciaPago(datos.getReferenciaPago());
        return usuarioRepository.save(existente);
    }

    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario"));
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
    }

    public Usuario asignarRol(Long idUsuario, Long idRol) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario"));
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new EntityNotFoundException("No existe el rol"));
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    public LoginResponseDTO iniciarSesion(String email, String contrasenia) {
        if (email == null || contrasenia == null) {
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(email))
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(contrasenia, usuario.getContraseniaHash())) {
            throw new CredencialesInvalidasException();
        }
        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new UsuarioInactivoException();
        }

        String rol = usuario.getRol() != null ? usuario.getRol().getNombre().name() : NombreRol.CLIENTE.name();
        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getId(), rol);
        return new LoginResponseDTO(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), rol);
    }

    public void cerrarSesion(String token) {
        if (token == null || token.isEmpty() || !jwtUtil.validarToken(token)) {
            throw new IllegalArgumentException("Token inválido");
        }
    }

    private void validarDatos(UsuarioRequestDTO datos, boolean contraseniaObligatoria) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (datos.getEmail() == null || !normalizarEmail(datos.getEmail()).matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }
        if (contraseniaObligatoria && (datos.getContrasenia() == null || datos.getContrasenia().isBlank())) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
    public void activarUsuario(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No existe el usuario"));
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuarioRepository.save(usuario);
    }
}
