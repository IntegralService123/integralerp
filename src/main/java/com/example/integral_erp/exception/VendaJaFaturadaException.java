package com.example.integral_erp.exception;

public class VendaJaFaturadaException extends DomainException {

    public VendaJaFaturadaException() {
        super("Esta venda já foi faturada.");
    }
}
