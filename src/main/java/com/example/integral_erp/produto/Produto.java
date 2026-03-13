package com.example.integral_erp.produto;

import java.math.BigDecimal;

import com.example.integral_erp.categoria.Categoria;
import com.example.integral_erp.common.Auditavel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "produto")
public class Produto extends Auditavel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(unique = true)
    private String codigoBarras;

    private BigDecimal preco;

    private Integer estoqueMinimo;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
