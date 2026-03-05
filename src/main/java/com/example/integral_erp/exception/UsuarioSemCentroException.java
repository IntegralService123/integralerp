package com.example.integral_erp.exception;

public class UsuarioSemCentroException extends DomainException {

    public UsuarioSemCentroException() {
        super("Usuário não possui centro associado.");
    }
}
