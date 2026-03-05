package com.example.integral_erp.estoque;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.estoque.dto.EstoqueAlertaResponse;
import com.example.integral_erp.estoque.dto.EstoqueResponse;
import com.example.integral_erp.exception.EstoqueNaoEncontradoException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listarTodos() {

        if (SecurityUtils.getRole() == Role.BASE_ADMIN) {

            return estoqueRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
        }

        Long centroId = SecurityUtils.getCentroId();

        return estoqueRepository.findByCentroId(centroId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listarPorCentro(Long centroId) {

        if (SecurityUtils.getRole() != Role.BASE_ADMIN) {

            Long centroUsuario = SecurityUtils.getCentroId();

            if (!centroUsuario.equals(centroId)) {
                throw new AccessDeniedException("Acesso negado.");
            }
        }

        return estoqueRepository.findByCentroId(centroId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public EstoqueResponse buscar(Long centroId, Long produtoId) {

        var estoque = estoqueRepository
                .findByProdutoIdAndCentroId(produtoId, centroId)
                .orElseThrow(() -> new EstoqueNaoEncontradoException());

        return toResponse(estoque);
    }

    @Transactional(readOnly = true)
    public List<EstoqueAlertaResponse> listarAbaixoDoMinimo() {

        return estoqueRepository.findAll().stream()
            .filter(e -> e.getProduto().getEstoqueMinimo() != null)
            .filter(e -> e.getQuantidade() <= e.getProduto().getEstoqueMinimo())
            .map(e -> new EstoqueAlertaResponse(
                    e.getProduto().getId(),
                    e.getProduto().getNome(),
                    e.getCentro().getId(),
                    e.getCentro().getNome(),
                    e.getQuantidade(),
                    e.getProduto().getEstoqueMinimo()
            ))
            .toList();
    }

    private EstoqueResponse toResponse(Estoque estoque) {

        return new EstoqueResponse(
                estoque.getProduto().getId(),
                estoque.getProduto().getNome(),
                estoque.getCentro().getId(),
                estoque.getCentro().getNome(),
                estoque.getQuantidade()
        );
    }
}
