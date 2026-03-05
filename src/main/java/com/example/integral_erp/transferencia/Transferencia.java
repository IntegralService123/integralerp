package com.example.integral_erp.transferencia;

import java.time.LocalDateTime;
import java.util.List;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.enums.StatusTransferencia;
import com.example.integral_erp.transferenciaitem.TransferenciaItem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transferencia")
public class Transferencia extends Auditavel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo; //Ex: TRF-2026-0001

    @ManyToOne
    private CentroDistribuicao origem;

    @ManyToOne
    private CentroDistribuicao destino;

    @Enumerated(EnumType.STRING)
    private StatusTransferencia status;

    private LocalDateTime dataConfirmacao;

    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL)
    private List<TransferenciaItem> itens;
}
