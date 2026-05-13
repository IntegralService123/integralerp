package com.example.integral_erp.categoria;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.categoria.dto.CategoriaRequestDTO;
import com.example.integral_erp.categoria.dto.CategoriaResponseDTO;
import com.example.integral_erp.produto.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> criar(
            @RequestBody CategoriaRequestDTO request) {

        return ResponseEntity.ok(categoriaService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id, @RequestBody Categoria dadosAtualizados) {
        return categoriaRepository.findById(id)
            .map(categoria -> {
                categoria.setNome(dadosAtualizados.getNome());
                categoria.setDescricao(dadosAtualizados.getDescricao());
                categoria.setImagemUrl(dadosAtualizados.getImagemUrl());
                Categoria atualizada = categoriaRepository.save(categoria);
                return ResponseEntity.ok(atualizada);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // MÉTODO EXCLUIR COM VALIDAÇÃO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        // 1. Verifica se a categoria existe
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // 2. Verifica se existem produtos vinculados
        if (produtoRepository.existsByCategoriaId(id)) {
            return ResponseEntity.badRequest()
                .body("Não é possível excluir: existem produtos vinculados a esta categoria.");
        }

        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
