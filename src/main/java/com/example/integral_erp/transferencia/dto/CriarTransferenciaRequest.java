package com.example.integral_erp.transferencia.dto;

import java.util.List;

public record CriarTransferenciaRequest(
        Long centroOrigemId,
        Long centroDestinoId,
        List<ItemRequest> itens
) {

    public record ItemRequest(
            Long produtoId,
            Integer quantidade
    ) {}
}