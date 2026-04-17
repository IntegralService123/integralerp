package com.example.integral_erp.pagamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PixResponseDTO(
    String qrCode,
    String qrCodeBase64,
    BigDecimal valor,
    String status,
    LocalDateTime expiracao
) {}
