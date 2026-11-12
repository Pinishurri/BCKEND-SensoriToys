package com.sensoritoys.ms_productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDashboardDTO {

    private Long productoId;
    private String nombreProducto;
    private Integer stockDisponible;
    private Boolean alertaStockBajo;
}
