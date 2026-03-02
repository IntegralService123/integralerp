package com.example.integral_erp.categoria;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.categoria.dto.CategoriaRequest;
import com.example.integral_erp.categoria.dto.CategoriaResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {

        if (categoriaRepository.findByNome(request.nome()).isPresent()) {
            throw new RuntimeException("Categoria já existe");
        }

        var categoria = new Categoria();
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());

        categoria = categoriaRepository.save(categoria);

        return toResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {

        var categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        return toResponse(categoria);
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao()
        );
    }
}
