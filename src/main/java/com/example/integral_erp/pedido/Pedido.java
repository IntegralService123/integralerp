package com.example.integral_erp.pedido;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.enums.StatusPedido;
import com.example.integral_erp.pedidoitem.PedidoItem;
import com.example.integral_erp.usuario.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "pedido")
public class Pedido extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal subtotal;

    private BigDecimal frete;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private String enderecoEntrega;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();

    public void adicionarItem(PedidoItem item) {
        item.setPedido(this);
        this.itens.add(item);
    }
}
