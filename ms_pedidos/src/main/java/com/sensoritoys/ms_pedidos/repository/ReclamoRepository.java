package com.sensoritoys.ms_pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sensoritoys.ms_pedidos.model.Reclamo;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    List<Reclamo> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    List<Reclamo> findAllByOrderByIdDesc();
}
