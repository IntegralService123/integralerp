package com.example.integral_erp.exception;

public class VendaNaoEncontradaException extends DomainException {

    public VendaNaoEncontradaException(Long id) {
        super("Venda com id " + id + " não encontrada.");
    }
}
