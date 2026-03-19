package com.example.integral_erp.pedidoitem.dto;

import java.math.BigDecimal;

public record PedidoItemResponseDTO(
    Long id,
    Long produtoId,
    String produtoNome,
    String imagemUrl,
    Integer quantidade,
    BigDecimal preco,
    BigDecimal subtotal
) {}