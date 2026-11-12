package com.sensoritoys.ms_usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    private String nombre;
    private String email;
    private String contrasenia;
    private String direccionEnvio;
    private String referenciaPago;
}
