package com.example.integral_erp.exception;

public class VendaCanceladaNaoPodeSerFaturadaException extends DomainException {

    public VendaCanceladaNaoPodeSerFaturadaException() {
        super("Venda cancelada não pode ser faturada.");
    }
}
