package com.example.integral_erp.pedido;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.carrinho.Carrinho;
import com.example.integral_erp.carrinho.CarrinhoRepository;
import com.example.integral_erp.carrinhoItem.CarrinhoItem;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.endereco.Endereco;
import com.example.integral_erp.endereco.dto.EnderecoResponseDTO;
import com.example.integral_erp.enums.FormaPagamento;
import com.example.integral_erp.enums.StatusPedido;
import com.example.integral_erp.enums.TipoPedido;
import com.example.integral_erp.movimentacaoestoque.MovimentacaoService;
import com.example.integral_erp.pagamento.PagamentoService;
import com.example.integral_erp.pedido.dto.ItemDTO;
import com.example.integral_erp.pedido.dto.PedidoManualRequestDTO;
import com.example.integral_erp.pedido.dto.PedidoRequestDTO;
import com.example.integral_erp.pedido.dto.PedidoResponseDTO;
import com.example.integral_erp.pedidoitem.PedidoItem;
import com.example.integral_erp.pedidoitem.dto.PedidoItemResponseDTO;
import com.example.integral_erp.produto.Produto;
import com.example.integral_erp.produto.ProdutoRepository;
import com.example.integral_erp.usuario.Usuario;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;
	private final MovimentacaoService movimentacaoService;
    private final PagamentoService pagamentoService;

    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();
        
        if (usuario == null) {
            throw new RuntimeException("Sessão expirada. Por favor, faça login novamente.");
        }

        Carrinho carrinho = carrinhoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);

        Endereco endereco = new Endereco();
        endereco.setCep(request.endereco().cep());
        endereco.setLogradouro(request.endereco().logradouro());
        endereco.setNumero(request.endereco().numero());
        endereco.setComplemento(request.endereco().complemento());
        endereco.setBairro(request.endereco().bairro());
        endereco.setCidade(request.endereco().cidade());
        endereco.setUf(request.endereco().uf());
        endereco.setApelido(request.endereco().apelido());
        endereco.setUsuario(usuario);

        pedido.setEndereco(endereco);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipo(TipoPedido.ECOMMERCE);

        List<PedidoItem> itens = carrinho.getItens()
                .stream()
                .map(item -> toPedidoItem(item, pedido))
                .toList();

        itens.forEach(pedido::adicionarItem);

        BigDecimal subtotal = itens.stream()
                .map(i -> i.getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setSubtotal(subtotal);
        pedido.setFrete(request.valorFrete());
        pedido.setTotal(subtotal.add(request.valorFrete()));

        pedidoRepository.save(pedido);

        pagamentoService.criarPagamento(pedido, FormaPagamento.valueOf(request.formaPagamento()));
        System.out.println(request.formaPagamento());

        // LIMPA CARRINHO
        carrinho.getItens().clear();
        carrinhoRepository.save(carrinho);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponseDTO criarManual(PedidoManualRequestDTO request) {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setClienteNome(request.clienteNome());
        pedido.setTipo(TipoPedido.MANUAL);

        List<PedidoItem> itens = request.itens().stream()
            .map(i -> criarItemManual(i, pedido))
            .toList();

        itens.forEach(pedido::adicionarItem);

        BigDecimal total = itens.stream()
            .map(i -> i.getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setSubtotal(total);
        pedido.setFrete(BigDecimal.ZERO);
        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        pagamentoService.processarPagamentoManual(pedido, FormaPagamento.valueOf(request.formaPagamento()));
        System.out.println(request.formaPagamento());

        // BAIXA ESTOQUE AUTOMÁTICA
        for (PedidoItem item : itens) {
            movimentacaoService.saidaVenda(
                item.getProdutoId(),
                item.getQuantidade()
            );
        }

        return toResponse(pedido);
    }

    public List<PedidoResponseDTO> listarMeusPedidos() {

        Long usuarioId = SecurityUtils.getUsuarioId();

        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PedidoResponseDTO buscarPorId(Long id) {

        Long usuarioId = SecurityUtils.getUsuarioId();

        Pedido pedido = pedidoRepository
                .findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        return toResponse(pedido);
    }

    public List<PedidoResponseDTO> listarTodos() {
    	
		return pedidoRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

	public PedidoResponseDTO buscarPorIdAdmin(Long id) {
    	
		Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

    	return toResponse(pedido);
	}

	@Transactional
	public PedidoResponseDTO atualizarStatus(Long id, String status) {

    	Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        StatusPedido novoStatus = StatusPedido.valueOf(status);

    	pedido.setStatus(novoStatus);

        if (novoStatus == StatusPedido.PAGO) {
            
			for (PedidoItem item : pedido.getItens()) {
				movimentacaoService.saidaVenda(item.getProdutoId(), item.getQuantidade());
			}
        }

    	return toResponse(pedido);
	}

    // ========================
    // MAPPERS
    // ========================

    private PedidoItem toPedidoItem(CarrinhoItem item, Pedido pedido) {

        Produto produto = item.getProduto();

        PedidoItem pedidoItem = new PedidoItem();
        pedidoItem.setProdutoId(produto.getId());
        pedidoItem.setProdutoNome(produto.getNome());
        pedidoItem.setImagemUrl(produto.getImagemUrl());
        pedidoItem.setPreco(produto.getPreco());
        pedidoItem.setQuantidade(item.getQuantidade());
        pedidoItem.setPedido(pedido);

        return pedidoItem;
    }

    private PedidoItem criarItemManual(ItemDTO item, Pedido pedido) {

        Produto produto = produtoRepository.findById(item.produtoId())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        PedidoItem pedidoItem = new PedidoItem();
        pedidoItem.setProdutoId(produto.getId());
        pedidoItem.setProdutoNome(produto.getNome());
        pedidoItem.setImagemUrl(produto.getImagemUrl());
        pedidoItem.setPreco(produto.getPreco());
        pedidoItem.setQuantidade(item.quantidade());
        pedidoItem.setPedido(pedido);

        return pedidoItem;
    }

    private PedidoResponseDTO toResponse(Pedido pedido) {

        List<PedidoItemResponseDTO> itens = pedido.getItens()
                .stream()
                .map(this::toItemResponse)
                .toList();

        EnderecoResponseDTO enderecoDTO = null;

        if (pedido.getEndereco() != null) {
            Endereco endereco = pedido.getEndereco();
            enderecoDTO = new EnderecoResponseDTO(
                endereco.getId(),
                endereco.getCep(),
                endereco.getLogradouro(),
                String.valueOf(endereco.getNumero()),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getApelido()
            );
        }
        

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getSubtotal(),
                pedido.getFrete(),
                pedido.getTotal(),
                pedido.getStatus().name(),
                enderecoDTO,
                itens
        );
    }

    private PedidoItemResponseDTO toItemResponse(PedidoItem item) {

        BigDecimal subtotal = item.getPreco()
                .multiply(BigDecimal.valueOf(item.getQuantidade()));

        return new PedidoItemResponseDTO(
                item.getId(),
                item.getProdutoId(),
                item.getProdutoNome(),
                item.getImagemUrl(),
                item.getQuantidade(),
                item.getPreco(),
                subtotal
        );
    }
}
