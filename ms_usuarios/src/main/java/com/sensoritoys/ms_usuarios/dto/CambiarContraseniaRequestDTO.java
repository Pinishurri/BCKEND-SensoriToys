package com.sensoritoys.ms_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CambiarContraseniaRequestDTO {

    private String contraseniaActual;
    private String contraseniaNueva;
} 