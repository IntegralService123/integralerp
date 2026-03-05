package com.example.integral_erp.exception;

public class EstoqueNaoEncontradoException extends DomainException {

    public EstoqueNaoEncontradoException() {
        super("Estoque não encontrado.");
    }

    public EstoqueNaoEncontradoException(String centroNome) {
        super("Estoque de " + centroNome + " não encontrado.");
    }
}
