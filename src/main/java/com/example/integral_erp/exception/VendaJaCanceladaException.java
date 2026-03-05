package com.example.integral_erp.exception;

public class VendaJaCanceladaException extends DomainException {

    public VendaJaCanceladaException() {
        super("Esta venda já foi cancelada.");
    }
}
