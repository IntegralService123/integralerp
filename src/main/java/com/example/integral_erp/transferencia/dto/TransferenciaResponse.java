package com.example.integral_erp.transferencia.dto;

import java.util.List;

import com.example.integral_erp.enums.StatusTransferencia;
import com.example.integral_erp.transferenciaitem.dto.TransferenciaItemResponse;

public record TransferenciaResponse(
        Long id,
        Long origemId,
        Long destinoId,
        StatusTransferencia status,
        List<TransferenciaItemResponse> itens
    ) {}
    

    

