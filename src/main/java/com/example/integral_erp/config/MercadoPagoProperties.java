package com.example.integral_erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "spring.mercadopago")
@Getter
@Setter
public class MercadoPagoProperties {

    private String accessToken;
}
