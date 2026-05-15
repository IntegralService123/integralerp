package com.example.integral_erp.produto;

import java.math.BigDecimal;

import com.example.integral_erp.categoria.Categoria;
import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.common.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    @Column(name = "nome_busca")
    private String nomeBusca;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(unique = true)
    private String codigoBarras;

    private BigDecimal preco;

    @Column(name = "preco_pix")
    private BigDecimal precoPix;

    private Integer estoqueMinimo;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    private String fabricante;

    private String material;

    private String unidade;

    @Column(precision = 10, scale = 3)
    private BigDecimal peso; // Em quilogramas (ex: 0.500 para 500g)

    private BigDecimal largura; // Em cm

    private BigDecimal altura; // Em cm

    private BigDecimal comprimento; // Em cm

    private BigDecimal diametro; // para produtos cilíndricos (tubos)

    @PrePersist
    @PreUpdate
    public void prepararNomeBusca() {
        if (this.nome != null) {
            this.nomeBusca = StringUtils.normalizarParaBusca(this.nome);
        }
    }

}
