package com.example.integral_erp.carrinho.dto;

import java.math.BigDecimal;
import java.util.List;

import com.example.integral_erp.carrinhoItem.dto.CarrinhoItemResponseDTO;

public record CarrinhoResponseDTO(
    Long id,
    List<CarrinhoItemResponseDTO> itens,
    BigDecimal total
) {}
