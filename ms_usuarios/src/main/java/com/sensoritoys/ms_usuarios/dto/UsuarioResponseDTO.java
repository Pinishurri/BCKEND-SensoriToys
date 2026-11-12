package com.sensoritoys.ms_usuarios.dto;

import java.time.LocalDateTime;

import com.sensoritoys.ms_usuarios.model.EstadoUsuario;
import com.sensoritoys.ms_usuarios.model.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private String direccionEnvio;
    private String referenciaPago;
    private EstadoUsuario estado;
    private LocalDateTime fechaCreacion;
    private String rol;

    public static UsuarioResponseDTO desde(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getDireccionEnvio(),
                usuario.getReferenciaPago(),
                usuario.getEstado(),
                usuario.getFechaCreacion(),
                usuario.getRol() != null ? usuario.getRol().getNombre().name() : null);
    }
}
