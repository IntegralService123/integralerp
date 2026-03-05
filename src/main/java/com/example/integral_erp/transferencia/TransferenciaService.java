package com.example.integral_erp.transferencia;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.*;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.StatusTransferencia;
import com.example.integral_erp.enums.TipoMovimentacao;
import com.example.integral_erp.estoque.*;
import com.example.integral_erp.exception.CentroDestinoNaoEncontradoException;
import com.example.integral_erp.exception.CentroOrigemNaoEncontradoException;
import com.example.integral_erp.exception.EstoqueInsuficienteException;
import com.example.integral_erp.exception.EstoqueNaoEncontradoException;
import com.example.integral_erp.exception.EstoqueOrigemNaoEncontradoException;
import com.example.integral_erp.exception.ProdutoNaoEncontradoException;
import com.example.integral_erp.exception.SomenteTransferenciaCriadaPodeSerCanceladaException;
import com.example.integral_erp.exception.TransferenciaConfirmadaOuCanceladaException;
import com.example.integral_erp.exception.TransferenciaNaoEncontradaException;
import com.example.integral_erp.movimentacaoestoque.MovimentacaoEstoque;
import com.example.integral_erp.produto.*;
import com.example.integral_erp.transferencia.dto.TransferenciaRequest;
import com.example.integral_erp.transferencia.dto.TransferenciaResponse;
import com.example.integral_erp.transferenciaitem.TransferenciaItem;
import com.example.integral_erp.transferenciaitem.TransferenciaItemRepository;
import com.example.integral_erp.transferenciaitem.dto.TransferenciaItemResponse;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaItemRepository itemRepository;
    private final CentroDistribuicaoRepository centroRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;

    @Transactional
    public Transferencia criarTransferencia(TransferenciaRequest request) {

        var origem = centroRepository.findById(request.centroOrigemId())
                .orElseThrow(() -> new CentroOrigemNaoEncontradoException());

        var destino = centroRepository.findById(request.centroDestinoId())
                .orElseThrow(() -> new CentroDestinoNaoEncontradoException());

        var transferencia = new Transferencia();
        transferencia.setOrigem(origem);
        transferencia.setDestino(destino);
        transferencia.setStatus(StatusTransferencia.CRIADA);

        transferencia = transferenciaRepository.save(transferencia);

        for (var itemRequest : request.itens()) {

            var produto = produtoRepository.findById(itemRequest.produtoId())
                .orElseThrow(() -> new ProdutoNaoEncontradoException(itemRequest.produtoId()));

            var estoqueOrigem = estoqueRepository
                    .findByProdutoIdAndCentroId(
                            produto.getId(),
                            origem.getId()
                    )
                    .orElseThrow(() -> new EstoqueNaoEncontradoException(origem.getNome()));

            if (estoqueOrigem.getQuantidade() < itemRequest.quantidade()) {
                throw new EstoqueInsuficienteException(produto.getNome());
            }

            // debita estoque da base
            estoqueOrigem.setQuantidade(
                    estoqueOrigem.getQuantidade() - itemRequest.quantidade()
            );

            estoqueRepository.save(estoqueOrigem);

            var item = new TransferenciaItem();
            item.setTransferencia(transferencia);
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade());

            itemRepository.save(item);

            transferencia.getItens().add(item);

            MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
            movimentacao.setProduto(produto);
            movimentacao.setCentro(origem);
            movimentacao.setTipo(TipoMovimentacao.SAIDA_TRANSFERENCIA);
            movimentacao.setQuantidade(itemRequest.quantidade());
            movimentacao.setReferenciaId(transferencia.getId());
            movimentacao.setUsuario(SecurityUtils.getUsuarioLogado());
        }
    
        return transferencia;
    }

    @Transactional
    public void confirmarTransferencia (Long transferenciaId) {
        
        var transferencia = transferenciaRepository.findById(transferenciaId)
                .orElseThrow(() -> new TransferenciaNaoEncontradaException(transferenciaId));

        if (transferencia.getStatus() != StatusTransferencia.CRIADA) {
                throw new TransferenciaConfirmadaOuCanceladaException();
        }

        var destino = transferencia.getDestino();

        for(var item : transferencia.getItens()) {
            var produto = item.getProduto();

            var estoqueDestino = estoqueRepository.findByProdutoIdAndCentroId(produto.getId(), destino.getId()).orElse(null);
        
            if (estoqueDestino == null) {
                estoqueDestino = new Estoque();
                estoqueDestino.setProduto(produto);
                estoqueDestino.setCentro(destino);
                estoqueDestino.setQuantidade(0);  
            }

            estoqueDestino.setQuantidade(estoqueDestino.getQuantidade() + item.getQuantidade());

            estoqueRepository.save(estoqueDestino);
        }

        transferencia.setStatus(StatusTransferencia.RECEBIDA);
        transferenciaRepository.save(transferencia);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setTipo(TipoMovimentacao.ENTRADA_TRANSFERENCIA);
    }

    public void cancelarTransferencia (Long transferenciaId) {
        
        var transferencia = transferenciaRepository.findById(transferenciaId)
            .orElseThrow(() -> new TransferenciaNaoEncontradaException(transferenciaId));

        if (transferencia.getStatus() != StatusTransferencia.CRIADA) {
            throw new SomenteTransferenciaCriadaPodeSerCanceladaException();
        }

        var origem = transferencia.getOrigem();

        for (var item : transferencia.getItens()) {

            var produto = item.getProduto();

            var estoqueOrigem = estoqueRepository
                .findByProdutoIdAndCentroId(produto.getId(), origem.getId())
                .orElseThrow(() -> new EstoqueOrigemNaoEncontradoException());

            estoqueOrigem.setQuantidade(
                estoqueOrigem.getQuantidade() + item.getQuantidade()
            );

            estoqueRepository.save(estoqueOrigem);
        }

        transferencia.setStatus(StatusTransferencia.CANCELADA);
        transferenciaRepository.save(transferencia);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setTipo(TipoMovimentacao.ESTORNO_TRANSFERENCIA);
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponse> listar() {
        return transferenciaRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransferenciaResponse buscarPorId(Long id) {

        var transferencia = transferenciaRepository.findById(id)
            .orElseThrow(() -> new TransferenciaNaoEncontradaException(id));

        return toResponse(transferencia);
    }

    public TransferenciaResponse toResponse(Transferencia transferencia) {

        var itens = transferencia.getItens().stream()
            .map(item -> new TransferenciaItemResponse(
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade()
            ))
            .toList();

        return new TransferenciaResponse(
            transferencia.getId(),
            transferencia.getOrigem().getId(),
            transferencia.getDestino().getId(),
            transferencia.getStatus(),
            itens
        );
    }
}
