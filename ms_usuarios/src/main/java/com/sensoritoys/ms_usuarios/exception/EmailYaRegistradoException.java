package com.sensoritoys.ms_usuarios.exception;

public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
