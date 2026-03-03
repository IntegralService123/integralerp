package com.example.integral_erp.vendaitem.dto;

import java.math.BigDecimal;

public record VendaItemResponse(
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {}
