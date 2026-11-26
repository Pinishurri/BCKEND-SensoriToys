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
    private String apellido;
    private String email;
    private String direccionEnvio;
    private String referenciaPago;
    private EstadoUsuario estado;
    private LocalDateTime fechaCreacion;
    private String rol;
    private String segundoApellido;
    private String calle;
    private String numero;
    private String deptoOficina;
    private String comuna;
    private String ciudad;
    private String region;

    public static UsuarioResponseDTO desde(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getDireccionEnvio(),
                usuario.getReferenciaPago(),
                usuario.getEstado(),
                usuario.getFechaCreacion(),
                usuario.getRol() != null ? usuario.getRol().getNombre().name() : null,
                usuario.getSegundoApellido(),
                usuario.getCalle(),
                usuario.getNumero(),
                usuario.getDeptoOficina(),
                usuario.getComuna(),
                usuario.getCiudad(),
                usuario.getRegion());
    }
}
