package com.sensoritoys.ms_usuarios.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sensoritoys.ms_usuarios.model.EstadoUsuario;
import com.sensoritoys.ms_usuarios.model.NombreRol;
import com.sensoritoys.ms_usuarios.model.Rol;
import com.sensoritoys.ms_usuarios.model.Usuario;
import com.sensoritoys.ms_usuarios.repository.RolRepository;
import com.sensoritoys.ms_usuarios.repository.UsuarioRepository;

@Component
@ConditionalOnProperty(name = "sensoritoys.datos-iniciales", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${sensoritoys.admin.email}") String adminEmail,
                           @Value("${sensoritoys.admin.password}") String adminPassword) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        Rol rolAdmin = obtenerOCrearRol(NombreRol.ADMIN);
        obtenerOCrearRol(NombreRol.CLIENTE);

        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            admin.setContraseniaHash(passwordEncoder.encode(adminPassword));
            admin.setEstado(EstadoUsuario.ACTIVO);
            admin.setFechaCreacion(LocalDateTime.now());
            admin.setRol(rolAdmin);
            usuarioRepository.save(admin);
        }
    }

    private Rol obtenerOCrearRol(NombreRol nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(null, nombre)));
    }
}
