package com.example.integral_erp.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

import com.example.integral_erp.endereco.dto.EnderecoResponseDTO;
import com.example.integral_erp.pedidoitem.dto.PedidoItemResponseDTO;

public record PedidoResponseDTO(
    Long id,
    BigDecimal subtotal,
    BigDecimal frete,
    BigDecimal total,
    String status,
    EnderecoResponseDTO endereco,
    List<PedidoItemResponseDTO> itens
) {}
