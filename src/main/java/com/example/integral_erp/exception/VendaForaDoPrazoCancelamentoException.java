package com.example.integral_erp.exception;

public class VendaForaDoPrazoCancelamentoException extends DomainException {

    public VendaForaDoPrazoCancelamentoException() {
        super("Venda só pode ser cancelada no mesmo dia da criação.");
    }
}
