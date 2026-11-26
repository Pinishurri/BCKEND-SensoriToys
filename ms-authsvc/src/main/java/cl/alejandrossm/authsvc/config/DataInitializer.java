package cl.alejandrossm.authsvc.config;

import cl.alejandrossm.authsvc.model.AppUser;
import cl.alejandrossm.authsvc.model.Role;
import cl.alejandrossm.authsvc.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUserIfMissing("admin", "admin", Role.ADMIN);
        createUserIfMissing("cliente", "1234", Role.CLIENTE);
    }

    private void createUserIfMissing(String username, String rawPassword, Role role) {
        if (repository.findByUsername(username).isEmpty()) {
            repository.save(new AppUser(username, passwordEncoder.encode(rawPassword), role));
        }
    }
}
