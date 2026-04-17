package com.example.integral_erp.pagamento;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        System.out.println("ID recebido: " + pedidoId);
        return pagamentoService.criarPagamentoCartao(pedidoId, dto);
    }

    @GetMapping("/{pedidoId}/status")
    public PagamentoStatusDTO status (@PathVariable Long pedidoId) {
        return pagamentoService.buscarStatus(pedidoId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> body) {

        try {
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            if (data != null && data.get("id") != null) {
                String paymentId = data.get("id").toString();

                PaymentClient client = new PaymentClient();
                Payment payment = client.get(Long.valueOf(paymentId));

                if ("approved".equals(payment.getStatus())) {
                    pagamentoService.confirmarPagamento(paymentId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.ok().build();
    }
}
