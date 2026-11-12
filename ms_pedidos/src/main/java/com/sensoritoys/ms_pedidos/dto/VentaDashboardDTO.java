package com.sensoritoys.ms_pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDashboardDTO {

    private Long pedidoId;
    private String nombreUsuario;
    private BigDecimal totalVenta;
    private LocalDateTime fechaRegistro;
    private String estadoVenta;
}
