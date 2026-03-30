package com.example.integral_erp.movimentacaoestoque.dto;

import java.time.LocalDateTime;

public record MovimentacaoResponseDTO(
    Long id,
    String produtoNome,
    String centroNome,
    String tipo,
    Integer quantidade,
    LocalDateTime createdAt
) {}
