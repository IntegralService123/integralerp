package com.example.integral_erp.venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicaoRepository;
import com.example.integral_erp.enums.StatusVenda;
import com.example.integral_erp.estoque.EstoqueRepository;
import com.example.integral_erp.produto.ProdutoRepository;
import com.example.integral_erp.venda.dto.VendaRequest;
import com.example.integral_erp.venda.dto.VendaResponse;
import com.example.integral_erp.vendaitem.VendaItem;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final CentroDistribuicaoRepository centroDistribuicaoRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final VendaRepository vendaRepository;

    @Transactional
    public VendaResponse criar (VendaRequest request) {

        var centro = centroDistribuicaoRepository.findById(request.centroId())
            .orElseThrow(() -> new RuntimeException("Centro não encontrado"));
            
        var venda = new Venda();
        venda.setCentro(centro);
        venda.setStatus(StatusVenda.FINALIZADA);
        venda.setDataVenda(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : request.itens()) {

            var produto = produtoRepository.findById(itemRequest.produtoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            var estoque = estoqueRepository.findByProdutoIdAndCentroId(produto.getId(), centro.getId())
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

            if (estoque.getQuantidade() < itemRequest.quantidade()) {
                throw new RuntimeException("Estoque insuficiente para produto: " + produto.getNome());
            }

            //Debita estoque
            estoque.setQuantidade(estoque.getQuantidade() - itemRequest.quantidade());

            estoqueRepository.save(estoque);

            var item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade());
            item.setValorUnitario(produto.getPreco());

            var subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemRequest.quantidade()));

            item.setSubtotal(subtotal);

            venda.getItens().add(item);

            total = total.add(subtotal);
        }

        venda.setValorTotal(total);

        venda = vendaRepository.save(venda);

        return new VendaResponse(
            venda.getId(),
            centro.getId(),
            venda.getStatus(),
            venda.getValorTotal()
        );
    }

    @Transactional
    public void cancelar(Long vendaId) {

        var venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new RuntimeException("Venda já está cancelada");
        }

        if (venda.getStatus() == StatusVenda.FATURADA) {
            throw new RuntimeException("Venda faturada não pode ser cancelada");
        }

        if (!venda.getDataVenda().toLocalDate().equals(LocalDate.now())) {
            throw new RuntimeException("Venda só pode ser cancelada no mesmo dia");
        }

        for (var item : venda.getItens()) {

            var estoque = estoqueRepository.findByProdutoIdAndCentroId(item.getProduto().getId(), venda.getCentro().getId())
                    .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

            // Devolve estoque
            estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());

            estoqueRepository.save(estoque);
        }

        venda.setStatus(StatusVenda.CANCELADA);

        vendaRepository.save(venda);
    }

    @Transactional
    public void faturar(Long vendaId) {

        var venda = vendaRepository.findById(vendaId)
            .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new RuntimeException("Venda cancelada não pode ser faturada");
        }

        if (venda.getStatus() == StatusVenda.FATURADA) {
            throw new RuntimeException("Venda já está faturada");
        }

        venda.setStatus(StatusVenda.FATURADA);

        vendaRepository.save(venda);
    }
}
