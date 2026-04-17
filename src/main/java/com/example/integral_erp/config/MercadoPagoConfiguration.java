package com.example.integral_erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${spring.mercadopago.access-token:}")
    private String accessToken;

    @PostConstruct
    public void init() {

        System.out.println("TOKEN: " + accessToken);

        if (accessToken != null && !accessToken.isBlank()) {
            com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);
        } else {
            System.out.println("Mercado Pago NÃO configurado");
        }
    }
}
