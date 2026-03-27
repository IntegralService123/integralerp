package com.example.integral_erp.movimentacaoestoque;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.enums.TipoMovimentacao;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "movimentacao_estoque",
    indexes = {
        @Index(name = "idx_mov_produto", columnList = "produto_id"),
        @Index(name = "idx_mov_centro", columnList = "centro_id")
    }
)
public class MovimentacaoEstoque extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private CentroDistribuicao centro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    private Long referenciaId; // id da venda ou transferencia

    private String referenciaTipo;

    @ManyToOne
    private Usuario usuario;
}
