package com.example.integral_erp.transferencia;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.*;
import com.example.integral_erp.enums.StatusTransferencia;
import com.example.integral_erp.estoque.*;
import com.example.integral_erp.produto.*;
import com.example.integral_erp.transferencia.dto.CriarTransferenciaRequest;
import com.example.integral_erp.transferenciaitem.TransferenciaItem;
import com.example.integral_erp.transferenciaitem.TransferenciaItemRepository;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaItemRepository itemRepository;
    private final CentroDistribuicaoRepository centroRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;

    @Transactional
    public Transferencia criarTransferencia(CriarTransferenciaRequest request) {

        var origem = centroRepository.findById(request.centroOrigemId())
                .orElseThrow(() -> new RuntimeException("Centro origem não encontrado"));

        var destino = centroRepository.findById(request.centroDestinoId())
                .orElseThrow(() -> new RuntimeException("Centro destino não encontrado"));

        var transferencia = new Transferencia();
        transferencia.setOrigem(origem);
        transferencia.setDestino(destino);
        transferencia.setStatus(StatusTransferencia.ENVIADA);

        transferencia = transferenciaRepository.save(transferencia);

        for (var itemRequest : request.itens()) {

            var produto = produtoRepository.findById(itemRequest.produtoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            var estoqueOrigem = estoqueRepository
                    .findByProdutoIdAndCentro(
                            produto.getId(),
                            origem.getId()
                    )
                    .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

            if (estoqueOrigem.getQuantidade() < itemRequest.quantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: "
                        + produto.getNome());
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
        }
    
        return transferencia;
    }

    @Transactional
    public void confirmarTransferencia (Long transferenciaId) {
        
        var transferencia = transferenciaRepository.findById(transferenciaId)
                .orElseThrow(() -> new RuntimeException("Transferência não encontrada"));

        if (transferencia.getStatus() != StatusTransferencia.ENVIADA) {
                throw new RuntimeException("Transferência já confirmada ou cancelada");
        }

        var destino = transferencia.getDestino();

        for(var item : transferencia.getItens()) {
            var produto = item.getProduto();

            var estoqueDestino = estoqueRepository.findByProdutoIdAndCentro(produto.getId(), destino.getId()).orElse(null);
        
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
    }
}
