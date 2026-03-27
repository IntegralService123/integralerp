package com.example.integral_erp.usuario.dto;

public record RegisterRequestDTO(
    String nome,
    String email,
    String senha
) {}