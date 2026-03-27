package com.example.integral_erp.movimentacaoestoque.dto;

public record EntradaMovimentacaoRequestDTO(
    Long produtoId,
    Long centroId,
    Integer quantidade
) {}
