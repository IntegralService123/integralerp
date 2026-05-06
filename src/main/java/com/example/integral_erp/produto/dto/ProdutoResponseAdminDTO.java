package com.example.integral_erp.produto.dto;

import java.math.BigDecimal;

public record ProdutoResponseAdminDTO(
        Long id,
        String nome,
        String descricao,
        String imagemUrl,
        String codigoBarras,
        Integer estoqueMinimo,
        Long categoriaId,
        String categoriaNome,
        BigDecimal preco,
        BigDecimal precoPix,
        Boolean ativo,
        String fabricante,
        String material,
        String unidade,
        BigDecimal peso,
        BigDecimal largura,
        BigDecimal altura,
        BigDecimal comprimento,
        BigDecimal diametro
) {}
