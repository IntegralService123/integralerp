package com.example.integral_erp.pedido.dto;

import java.util.List;

public record PedidoManualRequestDTO(
    List<ItemDTO> itens,
    String clienteNome,
    String formaPagamento
) {}
