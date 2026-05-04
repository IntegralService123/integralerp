package com.example.integral_erp.pagamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoletoResponseDTO(
    String barcode,
    String digitableLine,
    String pdfUrl,
    BigDecimal valor,
    String status,
    LocalDateTime dataVencimento
) {}
