package com.example.integral_erp.exception;

public class EstoqueInsuficienteException extends DomainException {

    public EstoqueInsuficienteException() {
        super("Estoque insuficiente para concluir a venda.");
    }

    public EstoqueInsuficienteException(String produtoNome) {
        super("Estoque insuficiente para o produto: " + produtoNome);
    }
}
