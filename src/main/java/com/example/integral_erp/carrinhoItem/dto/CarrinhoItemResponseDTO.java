package com.example.integral_erp.carrinhoItem.dto;

import java.math.BigDecimal;

public record CarrinhoItemResponseDTO( 
    Long id,
    Long produtoId,
    String produtoNome,
    String imagemUrl,
    Integer quantidade,
    BigDecimal preco,
    BigDecimal subtotal
) {}
