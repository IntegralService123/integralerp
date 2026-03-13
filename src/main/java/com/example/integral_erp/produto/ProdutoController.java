package com.example.integral_erp.produto;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.produto.dto.ProdutoRequest;
import com.example.integral_erp.produto.dto.ProdutoResponseAdminDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoResponseAdminDTO> criar(
            @RequestBody ProdutoRequest request) {

        return ResponseEntity.ok(produtoService.criar(request));
    }

    @GetMapping
    public List<ProdutoResponseAdminDTO> listar(
        @RequestParam(required = false) Long categoria,
        @RequestParam(required = false) String q
    ) {

        if (categoria != null) {
            return produtoService.listarPorCategoria(categoria);
        }

        return produtoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseAdminDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProdutoResponseAdminDTO>> listarPorCategoria(
            @PathVariable Long categoriaId) {

        return ResponseEntity.ok(
                produtoService.listarPorCategoria(categoriaId)
        );
    }

    @PutMapping("/{id}")
    public ProdutoResponseAdminDTO atualizar(
            @PathVariable Long id,
            @RequestBody ProdutoRequest request) {

        return produtoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        produtoService.excluir(id);
    }
}
