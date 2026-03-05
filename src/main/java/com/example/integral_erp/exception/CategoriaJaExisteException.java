package com.example.integral_erp.exception;

public class CategoriaJaExisteException extends DomainException {

    public CategoriaJaExisteException() {
        super("Categoria já existe");
    }
}
