package cl.alejandrossm.authsvc.service;

import cl.alejandrossm.authsvc.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adaptador entre los usuarios almacenados en H2 y el mecanismo de
 * autenticación de Spring Security.
 *
 * <p>Spring Security utiliza esta implementación de {@link UserDetailsService}
 * para obtener las credenciales y autoridades de un usuario antes de comparar
 * la contraseña enviada en el login con el hash BCrypt almacenado.</p>
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AppUserRepository repository;

    public DatabaseUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca el usuario por su nombre y lo convierte desde la entidad de la
     * aplicación al formato {@link UserDetails} que entiende Spring Security.
     *
     * @param username nombre de usuario recibido durante la autenticación
     * @return usuario con su contraseña cifrada y sus autoridades
     * @throws UsernameNotFoundException cuando el usuario no existe en H2
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        return repository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        // roles(...) agrega automáticamente el prefijo ROLE_.
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
