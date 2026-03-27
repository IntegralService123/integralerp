package com.example.integral_erp.produto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.categoria.CategoriaRepository;
import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.centrodistribuicao.CentroDistribuicaoRepository;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.enums.TipoCentro;
import com.example.integral_erp.estoque.Estoque;
import com.example.integral_erp.estoque.EstoqueRepository;
import com.example.integral_erp.produto.dto.ProdutoEstoqueResponseDTO;
import com.example.integral_erp.produto.dto.ProdutoRequest;
import com.example.integral_erp.produto.dto.ProdutoResponseAdminDTO;
import com.example.integral_erp.usuario.Usuario;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final CentroDistribuicaoRepository centroRepository;
    private final EstoqueRepository estoqueRepository;

// ======================================================
// CRIAR PRODUTO
// ======================================================

    @Transactional
    public ProdutoResponseAdminDTO criar(ProdutoRequest request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        // Apenas BASE_ADMIN pode criar produto
        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas BASE_ADMIN pode criar produto");
        }

        // Garante que existe centro BASE (regra do sistema)
        CentroDistribuicao centroBase = centroRepository
            .findByTipo(TipoCentro.BASE)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Centro BASE não encontrado"));

        var categoria = categoriaRepository.findById(request.categoriaId())
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        var produto = new Produto();
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setImagemUrl(request.imagemUrl());
        produto.setCodigoBarras(request.codigoBarras());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setPreco(request.preco());
        produto.setCategoria(categoria);
        produto.setAtivo(true);

        produto = produtoRepository.save(produto);

        return toResponse(produto);
    }

// ======================================================
// LISTAR PRODUTOS (CATÁLOGO)
// ======================================================

    @Transactional(readOnly = true)
    public List<ProdutoResponseAdminDTO> listar(
        Long categoriaId,
        Boolean incluirInativos
    ) {

        boolean somenteAtivos = incluirInativos == null || !incluirInativos;

        List<Produto> produtos;

        if (categoriaId != null) {
            produtos = somenteAtivos
                ? produtoRepository.findByCategoria_IdAndAtivoTrue(categoriaId)
                : produtoRepository.findByCategoria_Id(categoriaId);
        } else {
            produtos = somenteAtivos
                ? produtoRepository.findByAtivoTrue()
                : produtoRepository.findAll();
        }

        return produtos.stream()
            .map(this::toResponse)
            .toList();
    }

// ======================================================
// LISTAR ESTOQUE
// ======================================================

    @Transactional(readOnly = true)
    public List<ProdutoEstoqueResponseDTO> listarComEstoque(Long centroId, Long categoriaId, Boolean incluirInativos) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        boolean somenteAtivos = incluirInativos == null || !incluirInativos;

        Long centroFinal;

        if (usuario.getRole() == Role.BASE_ADMIN) {
            if (centroId == null) {
                throw new RuntimeException("Centro obrigatório para admin");
            }
            centroFinal = centroId;
        } else {
            centroFinal = usuario.getCentro().getId();
        }

        List<Produto> produtos;

        if (categoriaId != null) {
            produtos = somenteAtivos
                ? produtoRepository.findByCategoria_IdAndAtivoTrue(categoriaId)
                : produtoRepository.findByCategoria_Id(categoriaId);
        } else {
            produtos = somenteAtivos
                ? produtoRepository.findByAtivoTrue()
                : produtoRepository.findAll();
        }

        return produtos.stream().map(produto -> {

            Integer quantidade = estoqueRepository
                .findByProdutoIdAndCentroId(produto.getId(), centroFinal)
                .map(Estoque::getQuantidade)
                .orElse(0);

            return toEstoqueResponse(produto, quantidade);

        }).toList();
    }

// ======================================================
// BUSCAR POR ID
// ======================================================

    @Transactional(readOnly = true)
    public ProdutoResponseAdminDTO buscarPorId(Long id) {

        var produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        return toResponse(produto);
    }

// ======================================================
// ATUALIZAR
// ======================================================

    @Transactional
    public ProdutoResponseAdminDTO atualizar(Long id, ProdutoRequest request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas admin pode atualizar produto");
        }

        var produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        var categoria = categoriaRepository.findById(request.categoriaId())
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setImagemUrl(request.imagemUrl());
        produto.setCodigoBarras(request.codigoBarras());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setPreco(request.preco());
        produto.setCategoria(categoria);

        produto = produtoRepository.save(produto);

        return toResponse(produto);
    }

// ======================================================
// SOFT DELETE
// ======================================================

    @Transactional
    public void alternarStatus(Long id) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas admin pode alterar status");
        }

        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setAtivo(!produto.getAtivo());

        produtoRepository.save(produto);
    }

// ======================================================
// DELETE DEFINITIVO
// ======================================================

    @Transactional
    public void excluirDefinitivo(Long id) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas admin pode excluir produto");
        }

        produtoRepository.deleteById(id);
    }

// ======================================================
// MAPPER
// ======================================================

    private ProdutoResponseAdminDTO toResponse(Produto produto) {

        return new ProdutoResponseAdminDTO(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getImagemUrl(),
            produto.getCodigoBarras(),
            produto.getEstoqueMinimo(),
            produto.getCategoria().getId(),
            produto.getCategoria().getNome(),
            produto.getPreco(),
            produto.getAtivo()
        );
    }

    private ProdutoEstoqueResponseDTO toEstoqueResponse(
        Produto produto,
        Integer quantidade
    ) {
        return new ProdutoEstoqueResponseDTO(
            produto.getId(),
            produto.getNome(),
            produto.getCategoria().getNome(),
            produto.getEstoqueMinimo(),
            quantidade,
            produto.getAtivo()
        );
    }
}
