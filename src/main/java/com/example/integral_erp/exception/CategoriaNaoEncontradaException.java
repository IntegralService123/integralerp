package com.example.integral_erp.exception;

public class CategoriaNaoEncontradaException extends DomainException {

    public CategoriaNaoEncontradaException() {
        super("Categoria não encontrada");
    }
}
