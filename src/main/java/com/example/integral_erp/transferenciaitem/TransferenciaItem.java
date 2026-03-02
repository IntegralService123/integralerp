package com.example.integral_erp.transferenciaitem;

import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.transferencia.Transferencia;

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
@Table(name = "transferencia_item")
public class TransferenciaItem extends Auditavel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transferencia_id")
    private Transferencia transferencia;

    @ManyToOne
    private Produto produto;

    private Integer quantidade;
}
