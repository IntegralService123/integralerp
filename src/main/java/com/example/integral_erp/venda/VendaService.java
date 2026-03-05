package com.example.integral_erp.venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicaoRepository;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.enums.StatusVenda;
import com.example.integral_erp.estoque.EstoqueRepository;
import com.example.integral_erp.exception.CentroNaoEncontradoException;
import com.example.integral_erp.exception.EstoqueInsuficienteException;
import com.example.integral_erp.exception.EstoqueNaoEncontradoException;
import com.example.integral_erp.exception.ProdutoNaoEncontradoException;
import com.example.integral_erp.exception.UsuarioSemCentroException;
import com.example.integral_erp.exception.VendaCanceladaNaoPodeSerFaturadaException;
import com.example.integral_erp.exception.VendaForaDoPrazoCancelamentoException;
import com.example.integral_erp.exception.VendaJaCanceladaException;
import com.example.integral_erp.exception.VendaJaFaturadaException;
import com.example.integral_erp.exception.VendaNaoEncontradaException;
import com.example.integral_erp.produto.ProdutoRepository;
import com.example.integral_erp.venda.dto.VendaDetalhadaResponse;
import com.example.integral_erp.venda.dto.VendaRequest;
import com.example.integral_erp.venda.dto.VendaResponse;
import com.example.integral_erp.vendaitem.VendaItem;
import com.example.integral_erp.vendaitem.dto.VendaItemResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final CentroDistribuicaoRepository centroDistribuicaoRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final VendaRepository vendaRepository;

    @Transactional
    public VendaResponse criar (VendaRequest request) {

        Long centroId = SecurityUtils.getCentroId();

        var centro = centroDistribuicaoRepository.findById(centroId)
            .orElseThrow(() -> new CentroNaoEncontradoException(centroId));
            
        var venda = new Venda();
        venda.setCentro(centro);
        venda.setStatus(StatusVenda.FINALIZADA);
        venda.setDataVenda(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : request.itens()) {

            var produto = produtoRepository.findById(itemRequest.produtoId())
                .orElseThrow(() -> new ProdutoNaoEncontradoException(itemRequest.produtoId()));

            var estoque = estoqueRepository.findByProdutoIdAndCentroId(produto.getId(), centro.getId())
                .orElseThrow(() -> new EstoqueNaoEncontradoException(centro.getNome()));

            if (estoque.getQuantidade() < itemRequest.quantidade()) {
                throw new EstoqueInsuficienteException();
            }

            //Debita estoque
            estoque.setQuantidade(estoque.getQuantidade() - itemRequest.quantidade());

            estoqueRepository.save(estoque);

            var item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade());
            item.setValorUnitario(produto.getPreco());

            var subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemRequest.quantidade()));

            item.setSubtotal(subtotal);

            venda.getItens().add(item);

            total = total.add(subtotal);
        }

        venda.setValorTotal(total);

        venda = vendaRepository.save(venda);

        return new VendaResponse(
            venda.getId(),
            centro.getId(),
            venda.getStatus(),
            venda.getValorTotal()
        );
    }

    @Transactional
    public void cancelar(Long vendaId) {

        var venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        validarAcessoPorCentro(venda);

        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new VendaJaCanceladaException();
        }

        if (venda.getStatus() == StatusVenda.FATURADA) {
            throw new VendaJaFaturadaException();
        }

        if (!venda.getDataVenda().toLocalDate().equals(LocalDate.now())) {
            throw new VendaForaDoPrazoCancelamentoException();
        }

        for (var item : venda.getItens()) {

            var estoque = estoqueRepository.findByProdutoIdAndCentroId(item.getProduto().getId(), venda.getCentro().getId())
                    .orElseThrow(() -> new EstoqueNaoEncontradoException(venda.getCentro().getNome()));

            // Devolve estoque
            estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());

            estoqueRepository.save(estoque);
        }

        venda.setStatus(StatusVenda.CANCELADA);

        vendaRepository.save(venda);
    }

    @Transactional
    public void faturar(Long vendaId) {

        var venda = vendaRepository.findById(vendaId)
            .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        validarAcessoPorCentro(venda);

        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new VendaCanceladaNaoPodeSerFaturadaException();
        }

        if (venda.getStatus() == StatusVenda.FATURADA) {
            throw new VendaJaFaturadaException();
        }

        venda.setStatus(StatusVenda.FATURADA);

        vendaRepository.save(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaDetalhadaResponse> listar() {

        Role role = SecurityUtils.getRole();

        List<Venda> vendas;

        if (role == Role.BASE_ADMIN) {
            vendas = vendaRepository.findAll();
        } else {
            Long centroId = SecurityUtils.getCentroId();

            if (centroId == null) {
                throw new UsuarioSemCentroException();
            }

            vendas = vendaRepository.findByCentroId(centroId);
        }

        return vendas.stream()
                .map(this::toVendaDetalhadaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendaDetalhadaResponse buscarPorId(Long id) {

        var venda = vendaRepository.findById(id)
            .orElseThrow(() -> new VendaNaoEncontradaException(id));

        validarAcessoPorCentro(venda);

        return toVendaDetalhadaResponse(venda);
    }

    private void validarAcessoPorCentro(Venda venda) {

        Role role = SecurityUtils.getRole();

        if (role == Role.BASE_ADMIN) {
            return;
        }

        Long centroId = SecurityUtils.getCentroId();

        if (centroId == null ||
            !venda.getCentro().getId().equals(centroId)) {

            throw new AccessDeniedException("Acesso negado");
        }
    }

    private VendaDetalhadaResponse toVendaDetalhadaResponse(Venda venda) {

    var itens = venda.getItens().stream()
            .map(item -> new VendaItemResponse(
                    item.getProduto().getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    item.getValorUnitario(),
                    item.getSubtotal()
            ))
            .toList();

        return new VendaDetalhadaResponse(
            venda.getId(),
            venda.getCentro().getId(),
            venda.getCentro().getNome(),
            venda.getStatus(),
            venda.getDataVenda(),
            venda.getValorTotal(),
            itens
        );
    }
}
