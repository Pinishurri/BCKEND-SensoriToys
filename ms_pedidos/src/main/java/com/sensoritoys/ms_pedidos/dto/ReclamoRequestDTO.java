package com.sensoritoys.ms_pedidos.dto;

import com.sensoritoys.ms_pedidos.model.MotivoReclamo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamoRequestDTO {
    private Long usuarioId;
    private Long pedidoId;
    private MotivoReclamo motivo;
    private String descripcion;
}
