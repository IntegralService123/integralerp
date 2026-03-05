package com.example.integral_erp.exception;

public class TransferenciaConfirmadaOuCanceladaException extends DomainException {

    public TransferenciaConfirmadaOuCanceladaException() {
        super("Transferência já confirmada ou cancelada.");
    }

}
