package com.example.integral_erp.movimentacaoestoque;

import java.time.LocalDateTime;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.enums.TipoMovimentacao;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.usuario.Usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    @ManyToOne
    private CentroDistribuicao centro;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;

    private Integer quantidade;

    private Long referenciaId; // id da venda ou transferencia

    private LocalDateTime data;

    @ManyToOne
    private Usuario usuario;
}
