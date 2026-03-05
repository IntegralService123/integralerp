package com.example.integral_erp.exception;

public class CentroDestinoNaoEncontradoException extends DomainException {

    public CentroDestinoNaoEncontradoException() {
        super("Centro destino não encontrado");
    }
}
