package com.example.integral_erp.produto.dto;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        String codigoBarras,
        Integer estoqueMinimo,
        Long categoriaId,
        String categoriaNome
) {}
