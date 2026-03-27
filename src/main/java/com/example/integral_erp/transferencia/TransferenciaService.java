package com.example.integral_erp.transferencia;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.*;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.enums.StatusTransferencia;
import com.example.integral_erp.enums.TipoCentro;
import com.example.integral_erp.enums.TipoMovimentacao;
import com.example.integral_erp.estoque.*;
import com.example.integral_erp.movimentacaoestoque.MovimentacaoEstoque;
import com.example.integral_erp.movimentacaoestoque.MovimentacaoEstoqueRepository;
import com.example.integral_erp.produto.*;
import com.example.integral_erp.transferencia.dto.ConfirmarTransferenciaRequestDTO;
import com.example.integral_erp.transferencia.dto.TransferenciaRequestDTO;
import com.example.integral_erp.transferencia.dto.TransferenciaResponseDTO;
import com.example.integral_erp.transferenciaitem.TransferenciaItem;
import com.example.integral_erp.transferenciaitem.dto.TransferenciaItemResponseDTO;
import com.example.integral_erp.usuario.Usuario;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final CentroDistribuicaoRepository centroRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Transactional
    public TransferenciaResponseDTO criar(TransferenciaRequestDTO request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas admin pode criar transferência");
        }

        CentroDistribuicao origem = centroRepository
            .findByTipo(TipoCentro.BASE)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Centro BASE não encontrado"));

        CentroDistribuicao destino = centroRepository.findById(request.destinoId())
                .orElseThrow();

        Transferencia transferencia = new Transferencia();
        transferencia.setOrigem(origem);
        transferencia.setDestino(destino);
        transferencia.setStatus(StatusTransferencia.CRIADA);
        transferencia.setCodigo("TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        List<TransferenciaItem> itens = new ArrayList<>();

        for (var itemRequest : request.itens()) {

            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow();

            TransferenciaItem item = new TransferenciaItem();
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade());
            item.setTransferencia(transferencia);

            itens.add(item);
        }

        transferencia.setItens(itens);

        transferencia = transferenciaRepository.save(transferencia);

        return toResponse(transferencia);
    }

    @Transactional
    public TransferenciaResponseDTO enviar(Long id) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        if (usuario.getRole() != Role.BASE_ADMIN) {
            throw new RuntimeException("Apenas admin pode enviar");
        }

        Transferencia transferencia = transferenciaRepository.findById(id)
                .orElseThrow();

        if (transferencia.getStatus() != StatusTransferencia.CRIADA) {
            throw new RuntimeException("Transferência inválida para envio");
        }

        Long origemId = transferencia.getOrigem().getId();

        for (TransferenciaItem item : transferencia.getItens()) {

            Estoque estoqueOrigem = estoqueRepository
                    .findByProdutoIdAndCentroId(item.getProduto().getId(), origemId)
                    .orElseThrow(() -> new RuntimeException("Sem estoque"));

            if (estoqueOrigem.getQuantidade() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente");
            }

            estoqueOrigem.setQuantidade(
                estoqueOrigem.getQuantidade() - item.getQuantidade()
            );

            estoqueRepository.save(estoqueOrigem);

            MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
            movimentacao.setProduto(item.getProduto());
            movimentacao.setCentro(transferencia.getOrigem());
            movimentacao.setTipo(TipoMovimentacao.SAIDA_TRANSFERENCIA);
            movimentacao.setQuantidade(item.getQuantidade());
            movimentacao.setUsuario(usuario);
            movimentacao.setReferenciaId(transferencia.getId());

            movimentacaoEstoqueRepository.save(movimentacao);
        }

        transferencia.setDataEnvio(LocalDateTime.now());
        transferencia.setStatus(StatusTransferencia.ENVIADA);

        transferencia = transferenciaRepository.save(transferencia);

        return toResponse(transferencia);
    }

    @Transactional
    public TransferenciaResponseDTO confirmar(ConfirmarTransferenciaRequestDTO request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Transferencia transferencia = transferenciaRepository
                .findByCodigo(request.codigo())
                .orElseThrow();

        if (!transferencia.getDestino().getId().equals(usuario.getCentro().getId())) {
            throw new RuntimeException("Não pertence a este centro");
        }

        if (transferencia.getStatus() != StatusTransferencia.ENVIADA) {
            throw new RuntimeException("Transferência não foi enviada");
        }

        if (usuario.getRole() != Role.DISTRIBUIDOR) {
            throw new RuntimeException("Apenas distribuidores podem confirmar recebimento");
        }

        Long destinoId = transferencia.getDestino().getId();

        for (TransferenciaItem item : transferencia.getItens()) {

            Estoque estoqueDestino = estoqueRepository
                    .findByProdutoIdAndCentroId(item.getProduto().getId(), destinoId)
                    .orElseGet(() -> criarEstoque(item.getProduto(), transferencia.getDestino()));

            estoqueDestino.setQuantidade(
                estoqueDestino.getQuantidade() + item.getQuantidade()
            );

            estoqueRepository.save(estoqueDestino);

            MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
            movimentacao.setProduto(item.getProduto());
            movimentacao.setCentro(transferencia.getDestino());
            movimentacao.setTipo(TipoMovimentacao.ENTRADA_TRANSFERENCIA);
            movimentacao.setQuantidade(item.getQuantidade());
            movimentacao.setUsuario(usuario);
            movimentacao.setReferenciaId(transferencia.getId());

            movimentacaoEstoqueRepository.save(movimentacao);
        }

        transferencia.setDataRecebimento(LocalDateTime.now());
        transferencia.setStatus(StatusTransferencia.RECEBIDA);

        Transferencia salva = transferenciaRepository.save(transferencia);

        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponseDTO> listar() {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        List<Transferencia> lista;

        if (usuario.getRole() == Role.BASE_ADMIN) {

            lista = transferenciaRepository.findAll();
    
        } else {
            lista = transferenciaRepository.findByDestinoId(usuario.getCentro().getId());
        }

        return lista.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransferenciaResponseDTO buscarPorCodigo(String codigo) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Transferencia transferencia = transferenciaRepository
            .findByCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Transferência não encontrada"));

        if (usuario.getRole() == Role.DISTRIBUIDOR &&
            !transferencia.getDestino().getId().equals(usuario.getCentro().getId())) {

            throw new RuntimeException("Acesso negado");
        }

        return toResponse(transferencia);
    }

// =========================================================================================

    private Estoque criarEstoque(Produto produto, CentroDistribuicao centro) {

        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setCentro(centro);
        estoque.setQuantidade(0);

        return estoqueRepository.save(estoque);
    }

    private TransferenciaResponseDTO toResponse(Transferencia transferencia) {

        return new TransferenciaResponseDTO(
            transferencia.getId(),
            transferencia.getCodigo(),
            transferencia.getOrigem().getNome(),
            transferencia.getDestino().getNome(),
            transferencia.getStatus().name(),
            transferencia.getItens().stream().map(item ->
                new TransferenciaItemResponseDTO(
                    item.getProduto().getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade()
                )
            ).toList()
        );
    }
}
