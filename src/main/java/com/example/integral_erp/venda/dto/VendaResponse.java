package com.example.integral_erp.venda.dto;

import java.math.BigDecimal;

import com.example.integral_erp.enums.StatusVenda;

public record VendaResponse(
        Long id,
        Long centroId,
        StatusVenda status,
        BigDecimal valorTotal
) {}
