package cl.alejandrossm.authsvc.repository;

import java.util.Optional;

import cl.alejandrossm.authsvc.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
