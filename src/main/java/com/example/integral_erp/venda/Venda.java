package com.example.integral_erp.venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.vendaitem.VendaItem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CentroDistribuicao centro;

    private BigDecimal valorTotal;

    private LocalDateTime data;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
    private List<VendaItem> itens;
}
