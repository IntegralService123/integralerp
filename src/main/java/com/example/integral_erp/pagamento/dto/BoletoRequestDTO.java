package com.example.integral_erp.pagamento.dto;

public record BoletoRequestDTO(
    String nomePagador,
    String email,
    String cpfCnpj,
    String zipCode
) {}
