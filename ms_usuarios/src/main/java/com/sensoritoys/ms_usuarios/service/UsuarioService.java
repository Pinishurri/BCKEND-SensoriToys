package com.sensoritoys.ms_usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sensoritoys.ms_usuarios.config.JwtUtil;
import com.sensoritoys.ms_usuarios.config.UbicacionesChile;
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
        usuario.setApellido(datos.getApellido() == null ? null : datos.getApellido().trim());
        usuario.setEmail(email);
        usuario.setContraseniaHash(passwordEncoder.encode(datos.getContrasenia()));
        usuario.setReferenciaPago(datos.getReferenciaPago());
        aplicarDatosAdicionales(usuario, datos);
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
        existente.setApellido(datos.getApellido() == null ? null : datos.getApellido().trim());
        existente.setEmail(email);
        if (datos.getContrasenia() != null && !datos.getContrasenia().isBlank()) {
            existente.setContraseniaHash(passwordEncoder.encode(datos.getContrasenia()));
        }
        existente.setReferenciaPago(datos.getReferenciaPago());
        aplicarDatosAdicionales(existente, datos);
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

    public Usuario asignarRol(Long idUsuario, NombreRol nombreRol) {
        if (nombreRol == null) throw new IllegalArgumentException("El rol es obligatorio");
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario"));
        Rol rol = rolRepository.findByNombre(nombreRol)
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


    public void cambiarContrasenia(Long id, String contraseniaActual, String contraseniaNueva) {
    if (contraseniaActual == null || contraseniaActual.isBlank()) {
        throw new IllegalArgumentException("Debes ingresar tu contraseña actual");
    }

    if (contraseniaNueva == null || contraseniaNueva.length() < 8) {
        throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
    }

    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No existe el usuario"));

    if (!passwordEncoder.matches(contraseniaActual, usuario.getContraseniaHash())) {
        throw new IllegalArgumentException("La contraseña actual no es correcta");
    }

    usuario.setContraseniaHash(passwordEncoder.encode(contraseniaNueva));
    usuarioRepository.save(usuario);
}




    private void aplicarDatosAdicionales(Usuario usuario, UsuarioRequestDTO datos) {
        if (datos.getSegundoApellido() != null) {
            usuario.setSegundoApellido(limpiar(datos.getSegundoApellido()));
        }

        boolean direccionEstructurada = datos.getCalle() != null || datos.getNumero() != null
                || datos.getDeptoOficina() != null || datos.getComuna() != null || datos.getCiudad() != null
                || datos.getRegion() != null;

        if (!direccionEstructurada) {
            usuario.setDireccionEnvio(datos.getDireccionEnvio());
            return;
        }

        String calle = limpiar(datos.getCalle());
        String numero = limpiar(datos.getNumero());
        String deptoOficina = limpiar(datos.getDeptoOficina());
        String comuna = limpiar(datos.getComuna());
        String ciudad = limpiar(datos.getCiudad());
        String region = limpiar(datos.getRegion());

        boolean sinDatos = calle == null && numero == null && deptoOficina == null && comuna == null
                && ciudad == null && region == null;
        if (!sinDatos && (calle == null || numero == null || comuna == null || ciudad == null || region == null)) {
            throw new IllegalArgumentException("La dirección debe incluir calle, número, comuna, ciudad y región");
        }
        if (!sinDatos) {
            if (!UbicacionesChile.existeRegion(region)) {
                throw new IllegalArgumentException("La región seleccionada no es válida");
            }
            if (!UbicacionesChile.ciudadPerteneceARegion(region, ciudad)) {
                throw new IllegalArgumentException("La ciudad seleccionada no pertenece a la región");
            }
        }

        usuario.setCalle(calle);
        usuario.setNumero(numero);
        usuario.setDeptoOficina(deptoOficina);
        usuario.setComuna(comuna);
        usuario.setCiudad(ciudad);
        usuario.setRegion(region);
        usuario.setDireccionEnvio(sinDatos ? null : componerDireccion(calle, numero, deptoOficina, comuna, ciudad, region));
    }

    private String componerDireccion(String calle, String numero, String deptoOficina, String comuna,
                                     String ciudad, String region) {
        String complemento = deptoOficina == null ? "" : ", Casa/Depto/Of: " + deptoOficina;
        return calle + " #" + numero + complemento + ", " + comuna + ", " + ciudad + ", " + region;
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
