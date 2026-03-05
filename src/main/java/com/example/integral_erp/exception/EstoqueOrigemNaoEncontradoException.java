package com.example.integral_erp.exception;

public class EstoqueOrigemNaoEncontradoException extends DomainException {

    public EstoqueOrigemNaoEncontradoException() {
        super("Estoque origem não encontrado");
    }
}
