package com.example.integral_erp.venda.dto;

import java.util.List;

import com.example.integral_erp.vendaitem.dto.VendaItemRequest;

public record VendaRequest(
        Long centroId,
        List<VendaItemRequest> itens
) {}
