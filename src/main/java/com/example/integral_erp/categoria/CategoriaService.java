package com.example.integral_erp.categoria;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.categoria.dto.CategoriaRequestDTO;
import com.example.integral_erp.categoria.dto.CategoriaResponseDTO;
import com.example.integral_erp.exception.CategoriaJaExisteException;
import com.example.integral_erp.exception.CategoriaNaoEncontradaException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

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

    private CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getImagemUrl()
        );
    }
}
