package com.example.integral_erp.config.dto;

public record LoginResponseDTO(
    String token,
    Long id,
    String nome,
    String email,
    String role
) {}
