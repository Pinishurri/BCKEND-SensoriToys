package com.sensoritoys.ms_productos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sensoritoys.ms_productos.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    Optional<Producto> findByIdAndActivoTrue(Long id);

    boolean existsByCategoriaId(Long categoriaId);
}
