package com.example.integral_erp.transferencia.dto;

import java.util.List;

import com.example.integral_erp.transferenciaitem.dto.TransferenciaItemRequestDTO;

public record TransferenciaRequestDTO(
        Long destinoId,
        List<TransferenciaItemRequestDTO> itens
) {}