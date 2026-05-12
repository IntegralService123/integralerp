package com.example.integral_erp.produto.dto;

import java.math.BigDecimal;

public record ProdutoCatalogoDTO(
    Long id,
    String nome,
    BigDecimal preco,
    String imagemUrl,
    String categoriaNome,
    Integer estoqueDisponivel
) {}
