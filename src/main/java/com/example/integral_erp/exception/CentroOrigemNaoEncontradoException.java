package com.example.integral_erp.exception;

public class CentroOrigemNaoEncontradoException extends DomainException {

    public CentroOrigemNaoEncontradoException() {
        super("Centro origem não encontrado");
    }
}
