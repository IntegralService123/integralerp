package com.example.integral_erp.usuario.dto;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    String role,
    String centro
) {}
