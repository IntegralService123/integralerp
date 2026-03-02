package com.example.integral_erp.estoque;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.produto.Produto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "estoque",
       uniqueConstraints = @UniqueConstraint(columnNames = {"produto_id", "centro_id"}))
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "centro_id")
    private CentroDistribuicao centro;

    private Integer quantidade;
}
