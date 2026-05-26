package com.example.integral_erp.pagamento.dto;

public record PagamentoPixRequest(
    String nomePagador,
    String emailPagador,
    String cpfPagador
) {}
