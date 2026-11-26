package com.sensoritoys.ms_pedidos.dto;

import com.sensoritoys.ms_pedidos.model.EstadoReclamo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamoEstadoRequestDTO {
    private EstadoReclamo estado;
}
