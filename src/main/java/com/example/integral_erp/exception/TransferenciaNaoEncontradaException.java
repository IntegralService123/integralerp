package com.example.integral_erp.exception;

public class TransferenciaNaoEncontradaException extends DomainException {

    public TransferenciaNaoEncontradaException(Long id) {
        super("Transferência com id " + id + " não encontrada.");
    }
}
