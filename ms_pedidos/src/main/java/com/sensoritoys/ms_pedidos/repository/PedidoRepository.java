package com.sensoritoys.ms_pedidos.repository;

import com.sensoritoys.ms_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);
}