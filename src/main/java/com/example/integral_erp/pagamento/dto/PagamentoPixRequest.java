package com.example.integral_erp.pagamento.dto;

public record PagamentoPixRequest(
    String emailPagador,
    String cpfPagador
) {}
