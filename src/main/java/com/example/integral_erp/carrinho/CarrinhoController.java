package com.example.integral_erp.carrinho;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.carrinho.dto.CarrinhoRequestDTO;
import com.example.integral_erp.carrinho.dto.CarrinhoResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @PostMapping("/adicionar")
    public void adicionar(@RequestBody CarrinhoRequestDTO request) {

        carrinhoService.adicionarProduto(request.produtoId(), request.quantidade());
    }

    @GetMapping
    public CarrinhoResponseDTO buscar() {
        return carrinhoService.buscarCarrinho();
    }

    @DeleteMapping("/remover/{produtoId}")
    public void remover(@PathVariable Long produtoId) {
        carrinhoService.removerProduto(produtoId);
    }

    @PatchMapping("atualizar")
    public void atualizarQuantidade(@RequestParam Long produtoId, @RequestParam int quantidade) {
        carrinhoService.atualizarQuantidade(produtoId, quantidade);
    }
}
