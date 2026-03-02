package com.example.integral_erp.estoque.dto;

public record EstoqueAlertaResponse(
    Long produtoId,
    String produtoNome,
    Long centroId,
    String centroNome,
    Integer quantidade,
    Integer estoqueMinimo
) {}
