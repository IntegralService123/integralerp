package com.example.integral_erp.produto.dto;

public record ProdutoRequest(
        String nome,
        String descricao,
        String codigoBarras,
        Integer estoqueMinimo,
        Long categoriaId
) {}
