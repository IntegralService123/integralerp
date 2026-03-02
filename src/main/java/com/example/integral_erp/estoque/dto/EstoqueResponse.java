package com.example.integral_erp.estoque.dto;

public record EstoqueResponse(
        Long produtoId,
        String produtoNome,
        Long centroId,
        String centroNome,
        Integer quantidade
) {}
