package com.sensoritoys.ms_usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    private String nombre;
    private String apellido;
    private String email;
    private String contrasenia;
    private String direccionEnvio;
    private String referenciaPago;
    private String segundoApellido;
    private String calle;
    private String numero;
    private String deptoOficina;
    private String comuna;
    private String ciudad;
    private String region;

    // Compatibilidad con clientes y pruebas que aún usan el formato anterior.
    public UsuarioRequestDTO(String nombre, String email, String contrasenia, String direccionEnvio, String referenciaPago) {
        this(nombre, null, email, contrasenia, direccionEnvio, referenciaPago, null, null, null, null, null, null, null);
    }
}
