package com.sensoritoys.ms_usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sensoritoys.ms_usuarios.model.NombreRol;
import com.sensoritoys.ms_usuarios.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);
}
