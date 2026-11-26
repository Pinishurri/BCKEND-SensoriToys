package com.sensoritoys.ms_pedidos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sensoritoys.ms_pedidos.dto.ReclamoRequestDTO;
import com.sensoritoys.ms_pedidos.model.EstadoReclamo;
import com.sensoritoys.ms_pedidos.model.Pedido;
import com.sensoritoys.ms_pedidos.model.Reclamo;
import com.sensoritoys.ms_pedidos.repository.PedidoRepository;
import com.sensoritoys.ms_pedidos.repository.ReclamoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ReclamoService {

    private static final int LARGO_MINIMO_DESCRIPCION = 10;
    private static final int LARGO_MAXIMO_DESCRIPCION = 1000;

    private final ReclamoRepository reclamoRepository;
    private final PedidoRepository pedidoRepository;

    public ReclamoService(ReclamoRepository reclamoRepository, PedidoRepository pedidoRepository) {
        this.reclamoRepository = reclamoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Reclamo crearReclamo(ReclamoRequestDTO request) {
        if (request.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (request.getMotivo() == null) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        String descripcion = request.getDescripcion() == null ? "" : request.getDescripcion().trim();
        if (descripcion.length() < LARGO_MINIMO_DESCRIPCION) {
            throw new IllegalArgumentException(
                    "La descripción debe tener al menos " + LARGO_MINIMO_DESCRIPCION + " caracteres");
        }
        if (descripcion.length() > LARGO_MAXIMO_DESCRIPCION) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los " + LARGO_MAXIMO_DESCRIPCION + " caracteres");
        }
        if (request.getPedidoId() != null) {
            Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                    .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con id: " + request.getPedidoId()));
            if (!pedido.getUsuarioId().equals(request.getUsuarioId())) {
                throw new IllegalArgumentException("El pedido no pertenece al usuario");
            }
        }

        Reclamo reclamo = new Reclamo();
        reclamo.setUsuarioId(request.getUsuarioId());
        reclamo.setPedidoId(request.getPedidoId());
        reclamo.setMotivo(request.getMotivo());
        reclamo.setDescripcion(descripcion);
        reclamo.setEstado(EstadoReclamo.PENDIENTE);
        reclamo.setFechaCreacion(LocalDateTime.now());
        return reclamoRepository.save(reclamo);
    }

    @Transactional(readOnly = true)
    public List<Reclamo> listarReclamos() {
        return reclamoRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Reclamo> listarPorUsuario(Long usuarioId) {
        return reclamoRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
    }

    public Reclamo actualizarEstado(Long id, EstadoReclamo estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reclamo no encontrado con id: " + id));
        reclamo.setEstado(estado);
        return reclamoRepository.save(reclamo);
    }
}
