package com.example.integral_erp.carrinho;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.carrinho.dto.CarrinhoResponseDTO;
import com.example.integral_erp.carrinhoItem.CarrinhoItem;
import com.example.integral_erp.carrinhoItem.dto.CarrinhoItemResponseDTO;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.produto.ProdutoRepository;
import com.example.integral_erp.usuario.Usuario;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;

    public void adicionarProduto(Long produtoId, Integer quantidade) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Carrinho carrinho = carrinhoRepository
                .findByUsuarioId(usuario.getId())
                .orElseGet(() -> criarCarrinho(usuario));

        Produto produto = produtoRepository
                .findById(produtoId)
                .orElseThrow();

        Optional<CarrinhoItem> itemExistente = carrinho.getItens()
                .stream()
                .filter(i -> i.getProduto().getId().equals(produtoId))
                .findFirst();

        if (itemExistente.isPresent()) {

            CarrinhoItem item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + quantidade);

        } else {

            CarrinhoItem item = CarrinhoItem.builder()
                    .produto(produto)
                    .quantidade(quantidade)
                    .build();

            carrinho.adicionarItem(item);

        }

        carrinhoRepository.save(carrinho);
    }

    public CarrinhoResponseDTO buscarCarrinho() {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Carrinho carrinho = carrinhoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow();

        return toResponse(carrinho);
    }

    @Transactional
    public void removerProduto (Long produtoId) {

        Long usuarioId = SecurityUtils.getUsuarioId();

        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
			throw new RuntimeException("Carrinho vazio");
        }

		CarrinhoItem item = carrinho.getItens()
				.stream()
				.filter(i -> i.getProduto().getId().equals(produtoId))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("produto não está no carrinho"));

		carrinho.getItens().remove(item);

		carrinhoRepository.save(carrinho);
    }

	@Transactional
	public void atualizarQuantidade(Long produtoId, int quantidade) {

		Long usuarioId = SecurityUtils.getUsuarioId();

		Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuarioId)
				.orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

		CarrinhoItem item = carrinho.getItens()
				.stream()
				.filter(i -> i.getProduto().getId().equals(produtoId))
				.findFirst()
				.orElseThrow();

		if (quantidade <= 0) {
			carrinho.getItens().remove(item);
		} else {
			item.setQuantidade(quantidade);
		}

		carrinhoRepository.save(carrinho);
	}

    private Carrinho criarCarrinho(Usuario usuario) {

        Carrinho carrinho = Carrinho.builder()
                .usuario(usuario)
                .itens(new ArrayList<>())
                .build();

        return carrinhoRepository.save(carrinho);
    } 

    private CarrinhoResponseDTO toResponse(Carrinho carrinho) {

        List<CarrinhoItemResponseDTO> itens = carrinho.getItens() == null
                ? List.of()
                : carrinho.getItens()
                    .stream()
                    .map(this::toItemResponse)
                    .toList();

        BigDecimal total = itens.stream()
                .map(CarrinhoItemResponseDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarrinhoResponseDTO(
                carrinho.getId(),
                itens,
                total
        );
    }

    private CarrinhoItemResponseDTO toItemResponse(CarrinhoItem item) {

        Produto produto = item.getProduto();

        BigDecimal subtotal = produto.getPreco()
                .multiply(BigDecimal.valueOf(item.getQuantidade()));

        return new CarrinhoItemResponseDTO(
                item.getId(),
                produto.getId(),
                produto.getNome(),
                produto.getImagemUrl(),
                item.getQuantidade(),
                produto.getPreco(),
                subtotal
        );
    }

}
