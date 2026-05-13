package com.example.integral_erp.categoria;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.integral_erp.categoria.dto.CategoriaRequestDTO;
import com.example.integral_erp.categoria.dto.CategoriaResponseDTO;
import com.example.integral_erp.exception.CategoriaJaExisteException;
import com.example.integral_erp.exception.CategoriaNaoEncontradaException;
import com.example.integral_erp.produto.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO request) {

        if (categoriaRepository.findByNome(request.nome()).isPresent()) {
            throw new CategoriaJaExisteException();
        }

        var categoria = new Categoria();
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setImagemUrl(request.imagemUrl());

        categoria = categoriaRepository.save(categoria);

        return toResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {

        var categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException());

        return toResponse(categoria);
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria dadosNovos) {
        
        Categoria categoriaExistente = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada com o ID: " + id));

        // Atualiza os campos
        categoriaExistente.setNome(dadosNovos.getNome());
        categoriaExistente.setDescricao(dadosNovos.getDescricao());
        
        // Só atualiza a imagem se ela for enviada (evita sobrescrever com null)
        if (dadosNovos.getImagemUrl() != null && !dadosNovos.getImagemUrl().isEmpty()) {
            categoriaExistente.setImagemUrl(dadosNovos.getImagemUrl());
        }

        return categoriaRepository.save(categoriaExistente);
    }

    @Transactional
    public void excluir(Long id) {
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new RuntimeException("Categoria possui produtos vinculados.");
        }
        categoriaRepository.deleteById(id);
    }

    private CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getImagemUrl()
        );
    }
}
