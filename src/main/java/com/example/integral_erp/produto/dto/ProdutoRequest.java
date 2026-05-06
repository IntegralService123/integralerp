package com.example.integral_erp.produto.dto;

import java.math.BigDecimal;

public record ProdutoRequest(
        String nome,
        String descricao,
        String codigoBarras,
        Integer estoqueMinimo,
        Long categoriaId,
        String imagemUrl,
        BigDecimal preco,
        BigDecimal precoPix,
        Long centroId,
        String fabricante,
        String material,
        String unidade,
        BigDecimal peso,
        BigDecimal largura,
        BigDecimal altura,
        BigDecimal comprimento,
        BigDecimal diametro
) {}
