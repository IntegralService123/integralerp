package com.example.integral_erp.produto.dto;

import java.math.BigDecimal;

public record ProdutoResponseClienteDTO(
    String nome,
    BigDecimal preco
) {}
