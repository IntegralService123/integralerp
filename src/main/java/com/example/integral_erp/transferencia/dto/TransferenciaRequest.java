package com.example.integral_erp.transferencia.dto;

import java.util.List;

import com.example.integral_erp.transferenciaitem.dto.TransferenciaItemRequest;

public record TransferenciaRequest(
        Long centroOrigemId,
        Long centroDestinoId,
        List<TransferenciaItemRequest> itens
) {

    
}