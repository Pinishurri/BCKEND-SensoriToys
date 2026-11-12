package com.sensoritoys.ms_usuarios.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
