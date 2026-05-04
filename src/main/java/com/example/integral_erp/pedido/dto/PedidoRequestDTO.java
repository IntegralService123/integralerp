package com.example.integral_erp.pedido.dto;

import java.math.BigDecimal;

import com.example.integral_erp.endereco.dto.EnderecoRequestDTO;

public record PedidoRequestDTO(
    EnderecoRequestDTO endereco,
    String formaPagamento,
    BigDecimal valorFrete,
    String transportadora
) {}
