package com.example.integral_erp.carrinho;

import java.util.ArrayList;
import java.util.List;

import com.example.integral_erp.carrinhoItem.CarrinhoItem;
import com.example.integral_erp.common.Auditavel;
import com.example.integral_erp.usuario.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "carrinho")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrinho extends Auditavel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CarrinhoItem> itens = new ArrayList<>();

    public void adicionarItem(CarrinhoItem item) {
        itens.add(item);
        item.setCarrinho(this);
    }
}
