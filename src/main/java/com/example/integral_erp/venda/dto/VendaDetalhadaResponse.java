package com.example.integral_erp.venda.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.integral_erp.enums.StatusVenda;
import com.example.integral_erp.vendaitem.dto.VendaItemResponse;

public record VendaDetalhadaResponse(
        Long id,
        Long centroId,
        String centroNome,
        StatusVenda status,
        LocalDateTime dataVenda,
        BigDecimal valorTotal,
        List<VendaItemResponse> itens
) {}
