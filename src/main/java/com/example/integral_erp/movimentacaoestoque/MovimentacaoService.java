package com.example.integral_erp.movimentacaoestoque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.centrodistribuicao.CentroDistribuicaoRepository;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.enums.TipoCentro;
import com.example.integral_erp.enums.TipoMovimentacao;
import com.example.integral_erp.estoque.Estoque;
import com.example.integral_erp.estoque.EstoqueRepository;
import com.example.integral_erp.movimentacaoestoque.dto.EntradaMovimentacaoRequestDTO;
import com.example.integral_erp.movimentacaoestoque.dto.MovimentacaoResponseDTO;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.produto.ProdutoRepository;
import com.example.integral_erp.usuario.Usuario;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final CentroDistribuicaoRepository centroDistribuicaoRepository;

// ======================================================
// ENTRADA DE ESTOQUE (COMPRA / AJUSTE)
// ======================================================

    @Transactional
    public MovimentacaoResponseDTO entrada(EntradaMovimentacaoRequestDTO request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        // Apenas BASE_ADMIN pode dar entrada
        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas BASE_ADMIN pode dar entrada de estoque");
        }

        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas BASE_ADMIN pode dar entrada");
        }
        
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        CentroDistribuicao centro;

        if (usuario.getRole() == Role.BASE_ADMIN) {
            centro = centroDistribuicaoRepository
                .findByTipo(TipoCentro.BASE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Centro BASE não encontrado"));
        } else {
            centro = usuario.getCentro();
        }

        // Busca ou cria estoque
        Estoque estoque = estoqueRepository
                .findByProdutoIdAndCentroId(produto.getId(), centro.getId())
                .orElseGet(() -> criarEstoque(produto, centro));

        // SOMA estoque
        estoque.setQuantidade(
            estoque.getQuantidade() + request.quantidade()
        );

        estoqueRepository.save(estoque);

        // REGISTRA movimentação
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setCentro(centro);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA_COMPRA);
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setUsuario(usuario);

        movimentacao = movimentacaoRepository.save(movimentacao);

        return toResponse(movimentacao);
    }

// =========================================================================

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> listar(String tipo, Long produtoId, int page, int size) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<MovimentacaoEstoque> resultado;

        TipoMovimentacao tipoEnum = null;

        if (tipo != null && !tipo.isBlank()) {
            try {
                tipoEnum = TipoMovimentacao.valueOf(tipo);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Tipo de movimentação inválido");
            }
        }

        if (usuario.getRole() == Role.BASE_ADMIN) {
            resultado = movimentacaoRepository.buscarComFiltros(tipoEnum, produtoId, pageable);
        } else {
            resultado = movimentacaoRepository.buscarComFiltrosEPorCentro(tipoEnum, produtoId, usuario.getCentro().getId(), pageable);
        }

        return resultado.map(this::toResponse);
    }

// ======================================================
// HELPERS
// ======================================================

    private Estoque criarEstoque(Produto produto, CentroDistribuicao centro) {

        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setCentro(centro);
        estoque.setQuantidade(0);

        return estoqueRepository.save(estoque);
    }

    private MovimentacaoResponseDTO toResponse(MovimentacaoEstoque movimentacao) {

        return new MovimentacaoResponseDTO(
            movimentacao.getId(),
            movimentacao.getProduto().getNome(),
            movimentacao.getCentro().getNome(),
            movimentacao.getTipo().name(),
            movimentacao.getQuantidade(),
            movimentacao.getCreatedAt()
        );
    }
}
