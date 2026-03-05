package com.example.integral_erp.exception;

public class SomenteTransferenciaCriadaPodeSerCanceladaException extends DomainException {

    public SomenteTransferenciaCriadaPodeSerCanceladaException() {
        super("Somente transferências criadas podem ser canceladas");
    }
}
