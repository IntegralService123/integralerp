package com.example.integral_erp.config.dto;

public record UsuarioMeResponseDTO(
    Long id,
    String nome,
    String email,
    String role,
    Long centroId
) {}
