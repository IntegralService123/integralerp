package com.example.integral_erp.pagamento.dto;

public record CartaoPagamentoRequestDTO(
    Long pedidoId,
    String token,
    //String tipo,
    String cpf,
    String paymentMethodId,
    Integer installments,
    String email
) {}
