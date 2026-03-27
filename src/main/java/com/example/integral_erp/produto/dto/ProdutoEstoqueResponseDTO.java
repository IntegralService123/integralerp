package com.example.integral_erp.produto.dto;

public record ProdutoEstoqueResponseDTO(
    Long id,
    String nome,
    String categoriaNome,
    Integer estoqueMinimo,
    Integer quantidade,
    Boolean ativo
) {}
