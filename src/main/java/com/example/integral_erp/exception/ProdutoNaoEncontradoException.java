package com.example.integral_erp.exception;

public class ProdutoNaoEncontradoException extends DomainException {

    public ProdutoNaoEncontradoException(Long id) {
        super("Produto com id " + id + " não encontrado.");
    }

    public ProdutoNaoEncontradoException(String produtoNome) {
        super("Produto " + produtoNome + "não encontrada");
    }
}
