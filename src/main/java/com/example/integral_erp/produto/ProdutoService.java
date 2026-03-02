package com.example.integral_erp.produto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.categoria.CategoriaRepository;
import com.example.integral_erp.produto.dto.ProdutoRequest;
import com.example.integral_erp.produto.dto.ProdutoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {

        var categoria = categoriaRepository.findById(request.categoriaId())
            .orElseThrow(() -> new RuntimeException("Produto não encontrada"));

        var produto = new Produto();
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCodigoBarras(request.codigoBarras());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setCategoria(categoria);

        produto = produtoRepository.save(produto);

        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {

        var produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarPorCategoria(Long categoriaId) {

        return produtoRepository.findByCategoria_Id(categoriaId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private ProdutoResponse toResponse(Produto produto) {
        
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getCodigoBarras(),
            produto.getEstoqueMinimo(),
            produto.getCategoria().getId(),
            produto.getCategoria().getNome()
        );
    }
}
