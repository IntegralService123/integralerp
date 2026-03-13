package com.example.integral_erp.produto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.categoria.CategoriaRepository;
import com.example.integral_erp.exception.CategoriaNaoEncontradaException;
import com.example.integral_erp.exception.ProdutoNaoEncontradoException;
import com.example.integral_erp.produto.dto.ProdutoRequest;
import com.example.integral_erp.produto.dto.ProdutoResponseAdminDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public ProdutoResponseAdminDTO criar(ProdutoRequest request) {

        var categoria = categoriaRepository.findById(request.categoriaId())
            .orElseThrow(() -> new ProdutoNaoEncontradoException(request.nome()));

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
    public List<ProdutoResponseAdminDTO> listar() {
        return produtoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseAdminDTO buscarPorId(Long id) {

        var produto = produtoRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException());

        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseAdminDTO> listarPorCategoria(Long categoriaId) {

        return produtoRepository.findByCategoria_Id(categoriaId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ProdutoResponseAdminDTO atualizar(Long id, ProdutoRequest request) {

        var produto = produtoRepository.findById(id)
            .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado"));

        var categoria = categoriaRepository.findById(request.categoriaId())
            .orElseThrow(() -> new CategoriaNaoEncontradaException());

        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCodigoBarras(request.codigoBarras());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setCategoria(categoria);

        produto = produtoRepository.save(produto);

        return toResponse(produto);
    }

    @Transactional
    public void excluir(Long id) {

        if (!produtoRepository.existsById(id)) {
            throw new ProdutoNaoEncontradoException("Produto não encontrado");
        }

        produtoRepository.deleteById(id);
    }

    private ProdutoResponseAdminDTO toResponse(Produto produto) {
        
        return new ProdutoResponseAdminDTO(
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
