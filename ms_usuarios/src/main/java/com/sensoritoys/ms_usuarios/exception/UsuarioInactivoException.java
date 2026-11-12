package com.sensoritoys.ms_usuarios.exception;

public class UsuarioInactivoException extends RuntimeException {

    public UsuarioInactivoException() {
        super("Usuario inactivo");
    }
}
