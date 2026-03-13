package com.example.integral_erp.produto.dto;

public record ProdutoResponseAdminDTO(
        Long id,
        String nome,
        String descricao,
        String imagemUrl,
        String codigoBarras,
        Integer estoqueMinimo,
        Long categoriaId,
        String categoriaNome
) {}
