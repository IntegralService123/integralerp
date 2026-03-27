package com.example.integral_erp.transferencia.dto;

import java.util.List;

import com.example.integral_erp.transferenciaitem.dto.TransferenciaItemResponseDTO;

public record TransferenciaResponseDTO(
        Long id,
        String codigo,
        String origem,
        String destino,
        String status,
        List<TransferenciaItemResponseDTO> itens
    ) {}
    

    

