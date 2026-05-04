package com.example.integral_erp.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.enums.FormaPagamento;
import com.example.integral_erp.enums.GatewayPagamento;
import com.example.integral_erp.enums.StatusPagamento;
import com.example.integral_erp.pedido.Pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
public class Pagamento extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayPagamento gateway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "linha_digitavel")
    private String linhaDigitavel;

    @Column(name = "transacao_gateway_id")
    private String transacaoGatewayId;

    @Column(name = "url_pagamento")
    private String urlPagamento;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    private LocalDateTime dataConfirmacao;

    private LocalDateTime dataExpiracao;
}
