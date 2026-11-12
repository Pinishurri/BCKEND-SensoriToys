package com.sensoritoys.ms_usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private Long id;
    private String nombre;
    private String email;
    private String rol;
}
