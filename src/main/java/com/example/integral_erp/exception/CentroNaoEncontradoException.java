package com.example.integral_erp.exception;

public class CentroNaoEncontradoException extends DomainException {

    public CentroNaoEncontradoException(Long id) {
        super("Centro com id " + id + " não encontrada.");
    }
}
