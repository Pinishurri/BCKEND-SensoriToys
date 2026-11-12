package com.sensoritoys.ms_pedidos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sensoritoys.ms_pedidos.dto.UsuarioDTO;
import com.sensoritoys.ms_pedidos.dto.VentaDashboardDTO;
import com.sensoritoys.ms_pedidos.model.DetallePedido;
import com.sensoritoys.ms_pedidos.model.Pedido;
import com.sensoritoys.ms_pedidos.repository.PedidoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class PedidoService {

    private static final String USUARIOS_URL = "http://localhost:8081/api/v1/usuarios/{id}";
    private static final String ESTADO_VENTA = "COMPLETADA";
    private static final String USUARIO_NO_DISPONIBLE = "Usuario no disponible";

    private final PedidoRepository pedidoRepository;
    private final RestTemplate restTemplate;

    public PedidoService(PedidoRepository pedidoRepository, RestTemplate restTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.restTemplate = restTemplate;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> obtenerPorId(Long id) {
        return pedidoRepository.findById(id);
    }
    public List<Pedido> listarPedidosPorUsuario(Long usuarioId) {
    return pedidoRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
    }

    public Pedido crearPedido(Pedido pedido) {
        if (pedido.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuarioId es obligatorio");
        }
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un detalle");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido detalle : pedido.getDetalles()) {
            total = total.add(calcularSubtotal(detalle));
            detalle.setId(null);
        }

        pedido.setId(null);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    public void eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new EntityNotFoundException("Pedido no encontrado con ID: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public List<VentaDashboardDTO> obtenerDashboardVentas() {
        Map<Long, String> nombresPorUsuario = new HashMap<>();

        return pedidoRepository.findAll().stream()
                .map(pedido -> new VentaDashboardDTO(
                        pedido.getId(),
                        nombresPorUsuario.computeIfAbsent(pedido.getUsuarioId(), this::consultarNombreUsuario),
                        pedido.getTotal(),
                        pedido.getFechaCreacion(),
                        ESTADO_VENTA))
                .toList();
    }

    private BigDecimal calcularSubtotal(DetallePedido detalle) {
        if (detalle.getPrecioUnitario() == null || detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("Cada detalle requiere cantidad mayor a 0 y precio unitario");
        }
        return detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
    }

    private String consultarNombreUsuario(Long usuarioId) {
        try {
            UsuarioDTO usuario = restTemplate.getForObject(USUARIOS_URL, UsuarioDTO.class, usuarioId);
            if (usuario != null && usuario.getNombre() != null) {
                return usuario.getNombre();
            }
            return USUARIO_NO_DISPONIBLE;
        } catch (RestClientException e) {
            return USUARIO_NO_DISPONIBLE;
        }
    }
}
