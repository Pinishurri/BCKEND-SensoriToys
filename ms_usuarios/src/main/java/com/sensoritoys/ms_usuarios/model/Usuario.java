package com.sensoritoys.ms_usuarios.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String apellido;

    @Column(unique = true, nullable = false)
    private String email;

    private String contraseniaHash;

    private String direccionEnvio;

    private String referenciaPago;

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estado;

    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    private String segundoApellido;

    private String calle;

    private String numero;

    private String deptoOficina;

    private String comuna;

    private String ciudad;

    private String region;

    // Conserva compatibilidad con el constructor previo a incorporar apellido.
    public Usuario(Long id, String nombre, String email, String contraseniaHash,
                   String direccionEnvio, String referenciaPago, EstadoUsuario estado,
                   LocalDateTime fechaCreacion, Rol rol) {
        this(id, nombre, null, email, contraseniaHash, direccionEnvio,
                referenciaPago, estado, fechaCreacion, rol, null, null, null, null, null, null, null);
    }
}
