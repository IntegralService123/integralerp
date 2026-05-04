package com.example.integral_erp.pagamento;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.pagamento.dto.BoletoResponseDTO;
import com.example.integral_erp.pagamento.dto.CartaoPagamentoRequestDTO;
import com.example.integral_erp.pagamento.dto.CartaoPagamentoResponseDTO;
import com.example.integral_erp.pagamento.dto.PagamentoStatusDTO;
import com.example.integral_erp.pagamento.dto.PixResponseDTO;
import com.example.integral_erp.pedido.Pedido;
import com.example.integral_erp.pedido.PedidoRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final PedidoRepository pedidoRepository;

    @PostMapping("/{pedidoId}/simular")
    public void simular (@PathVariable Long pedidoId) {
        pagamentoService.simularPagamento(pedidoId);
    }

    @PostMapping("/{pedidoId}/pix")
    public PixResponseDTO gerarPix(@PathVariable Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow();

        return pagamentoService.criarPagamentoPix(pedido);
    }

    @PostMapping("/{pedidoId}/cartao")
    public CartaoPagamentoResponseDTO pagarCartao(@PathVariable Long pedidoId, @RequestBody CartaoPagamentoRequestDTO dto) {

        System.out.println("======= ENTRADA CONTROLLER =======");
        System.out.println("Pedido ID: " + pedidoId);
        System.out.println("DTO recebido: " + dto);
        
        return pagamentoService.criarPagamentoCartao(pedidoId, dto);
    }

    @PostMapping("/{pedidoId}/boleto")
    public BoletoResponseDTO gerarBoleto(@PathVariable Long pedidoId) {
        
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        return pagamentoService.criarPagamentoBoleto(pedido);    
    }
    

    @GetMapping("/{pedidoId}/status")
    public PagamentoStatusDTO status (@PathVariable Long pedidoId) {
        return pagamentoService.buscarStatus(pedidoId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> body) {

        try {
            String action = (String) body.get("action");
            String type = (String) body.get("type");

            if ("payment".equals(type) || "payment.updated".equals(action)) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String paymentId = data.get("id").toString();

                PaymentClient client = new PaymentClient();
                Payment payment = client.get(Long.valueOf(paymentId));

                if ("approved".equals(payment.getStatus())) {
                    pagamentoService.processarNotificacaoGateway(paymentId);
                    System.out.println("SUCESSO: Pagamento aprovado processado.");
                } else {
                    System.out.println("AVISO: Pagamento ainda com status: " + payment.getStatus());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.ok().build();
    }
}
