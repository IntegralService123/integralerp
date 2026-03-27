package com.example.integral_erp.enums;

public enum TipoMovimentacao {
    
    // ENTRADAS
    ENTRADA_COMPRA,          // compra de fornecedor (BASE)
    ENTRADA_TRANSFERENCIA,   // recebimento de outro centro
    AJUSTE_ENTRADA,          // ajuste manual positivo

    // SAÍDAS
    SAIDA_TRANSFERENCIA,     // envio para outro centro
    SAIDA_VENDA,             // venda
    AJUSTE_SAIDA,            // ajuste manual negativo

    // REVERSÕES
    ESTORNO_TRANSFERENCIA,
    CANCELAMENTO_VENDA
}
