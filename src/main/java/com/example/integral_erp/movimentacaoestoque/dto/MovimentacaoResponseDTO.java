package com.example.integral_erp.movimentacaoestoque.dto;

public record MovimentacaoResponseDTO(
    Long id,
    String produtoNome,
    String centroNome,
    String tipo,
    Integer quantidade
) {}
