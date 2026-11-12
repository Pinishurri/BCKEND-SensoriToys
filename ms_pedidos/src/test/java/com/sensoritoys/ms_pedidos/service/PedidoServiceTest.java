package com.sensoritoys.ms_pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sensoritoys.ms_pedidos.dto.UsuarioDTO;
import com.sensoritoys.ms_pedidos.dto.VentaDashboardDTO;
import com.sensoritoys.ms_pedidos.model.DetallePedido;
import com.sensoritoys.ms_pedidos.model.Pedido;
import com.sensoritoys.ms_pedidos.repository.PedidoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void testCrearPedidoCalculaTotalCorrectamente() {
        DetallePedido detalle = new DetallePedido(null, 1L, 2, BigDecimal.valueOf(50.00));
        Pedido pedido = new Pedido(null, 10L, null, null, List.of(detalle));
        Pedido pedidoGuardado = new Pedido(1L, 10L, null, BigDecimal.valueOf(100.00), List.of(detalle));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        Pedido resultado = pedidoService.crearPedido(pedido);

        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(100.00), resultado.getTotal());
        assertEquals(BigDecimal.valueOf(100.00), pedido.getTotal());
        assertNotNull(pedido.getFechaCreacion());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testCrearPedidoSinDetallesLanzaExcepcion() {
        Pedido pedido = new Pedido(null, 10L, null, null, List.of());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.crearPedido(pedido));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void testListarPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(new Pedido()));

        List<Pedido> pedidos = pedidoService.listarPedidos();

        assertFalse(pedidos.isEmpty());
    }

    @Test
    void testEliminarPedidoInexistenteLanzaExcepcion() {
        when(pedidoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> pedidoService.eliminarPedido(99L));
        verify(pedidoRepository, never()).deleteById(99L);
    }

    @Test
    void testEliminarPedidoExistente() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);

        pedidoService.eliminarPedido(1L);

        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDashboardVentasAdjuntaNombreDeUsuario() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 27, 10, 0);
        Pedido pedido = new Pedido(1L, 10L, fecha, BigDecimal.valueOf(100.00), List.of());
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setNombre("Ana Pérez");

        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class), eq(10L))).thenReturn(usuario);

        List<VentaDashboardDTO> resultado = pedidoService.obtenerDashboardVentas();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getPedidoId());
        assertEquals("Ana Pérez", resultado.get(0).getNombreUsuario());
        assertEquals(BigDecimal.valueOf(100.00), resultado.get(0).getTotalVenta());
        assertEquals(fecha, resultado.get(0).getFechaRegistro());
    }

    @Test
    void testDashboardVentasConUsuarioNoDisponible() {
        Pedido pedido = new Pedido(1L, 10L, LocalDateTime.now(), BigDecimal.TEN, List.of());

        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class), eq(10L)))
                .thenThrow(new RestClientException("sin conexión"));

        List<VentaDashboardDTO> resultado = pedidoService.obtenerDashboardVentas();

        assertEquals("Usuario no disponible", resultado.get(0).getNombreUsuario());
    }
}
