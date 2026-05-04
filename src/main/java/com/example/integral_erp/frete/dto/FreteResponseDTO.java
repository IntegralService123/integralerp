package com.example.integral_erp.frete.dto;

import java.math.BigDecimal;

public record FreteResponseDTO(
    String nomeServico, // Ex: "SEDEX", "PAC", "Jadlog .Com"
    BigDecimal valor,
    Integer prazoEntrega, // Em dias
    String empresa // Ex: "Correios"
) {}
