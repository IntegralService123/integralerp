package com.example.integral_erp.pedido;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.carrinho.Carrinho;
import com.example.integral_erp.carrinho.CarrinhoRepository;
import com.example.integral_erp.carrinhoItem.CarrinhoItem;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.StatusPedido;
import com.example.integral_erp.pedido.dto.PedidoRequestDTO;
import com.example.integral_erp.pedido.dto.PedidoResponseDTO;
import com.example.integral_erp.pedidoitem.PedidoItem;
import com.example.integral_erp.pedidoitem.dto.PedidoItemResponseDTO;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.usuario.Usuario;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarrinhoRepository carrinhoRepository;

    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Carrinho carrinho = carrinhoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEnderecoEntrega(request.enderecoEntrega());
        pedido.setStatus(StatusPedido.PENDENTE);

        List<PedidoItem> itens = carrinho.getItens()
                .stream()
                .map(item -> toPedidoItem(item, pedido))
                .toList();

        itens.forEach(pedido::adicionarItem);

        BigDecimal subtotal = itens.stream()
                .map(i -> i.getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setSubtotal(subtotal);
        pedido.setFrete(BigDecimal.ZERO);
        pedido.setTotal(subtotal);

        pedidoRepository.save(pedido);

        // 🔥 LIMPA CARRINHO (essencial)
        carrinho.getItens().clear();
        carrinhoRepository.save(carrinho);

        return toResponse(pedido);
    }

    public List<PedidoResponseDTO> listarMeusPedidos() {

        Long usuarioId = SecurityUtils.getUsuarioId();

        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PedidoResponseDTO buscarPorId(Long id) {

        Long usuarioId = SecurityUtils.getUsuarioId();

        Pedido pedido = pedidoRepository
                .findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        return toResponse(pedido);
    }

    public List<PedidoResponseDTO> listarTodos() {
    	
		return pedidoRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

	public PedidoResponseDTO buscarPorIdAdmin(Long id) {
    	
		Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

    	return toResponse(pedido);
	}

	@Transactional
	public PedidoResponseDTO atualizarStatus(Long id, String status) {

    	Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

    	pedido.setStatus(StatusPedido.valueOf(status));

    	return toResponse(pedido);
	}

    // ========================
    // MAPPERS
    // ========================

    private PedidoItem toPedidoItem(CarrinhoItem item, Pedido pedido) {

        Produto produto = item.getProduto();

        PedidoItem pi = new PedidoItem();
        pi.setProdutoId(produto.getId());
        pi.setProdutoNome(produto.getNome());
        pi.setImagemUrl(produto.getImagemUrl());
        pi.setPreco(produto.getPreco());
        pi.setQuantidade(item.getQuantidade());
        pi.setPedido(pedido);

        return pi;
    }

    private PedidoResponseDTO toResponse(Pedido pedido) {

        List<PedidoItemResponseDTO> itens = pedido.getItens()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getSubtotal(),
                pedido.getFrete(),
                pedido.getTotal(),
                pedido.getStatus().name(),
                pedido.getEnderecoEntrega(),
                itens
        );
    }

    private PedidoItemResponseDTO toItemResponse(PedidoItem item) {

        BigDecimal subtotal = item.getPreco()
                .multiply(BigDecimal.valueOf(item.getQuantidade()));

        return new PedidoItemResponseDTO(
                item.getId(),
                item.getProdutoId(),
                item.getProdutoNome(),
                item.getImagemUrl(),
                item.getQuantidade(),
                item.getPreco(),
                subtotal
        );
    }
}
