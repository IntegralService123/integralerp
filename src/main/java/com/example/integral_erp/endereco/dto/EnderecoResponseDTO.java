package com.example.integral_erp.endereco.dto;

public record EnderecoResponseDTO(
    Long id,
    String cep,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String uf,
    String apelido
) {}
