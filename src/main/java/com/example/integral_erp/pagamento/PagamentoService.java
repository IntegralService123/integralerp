package com.example.integral_erp.pagamento;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.integral_erp.config.MercadoPagoProperties;
import com.example.integral_erp.enums.FormaPagamento;
import com.example.integral_erp.enums.GatewayPagamento;
import com.example.integral_erp.enums.StatusPagamento;
import com.example.integral_erp.enums.StatusPedido;
import com.example.integral_erp.movimentacaoestoque.MovimentacaoService;
import com.example.integral_erp.pagamento.dto.CartaoPagamentoRequestDTO;
import com.example.integral_erp.pagamento.dto.CartaoPagamentoResponseDTO;
import com.example.integral_erp.pagamento.dto.PagamentoStatusDTO;
import com.example.integral_erp.pagamento.dto.PixResponseDTO;
import com.example.integral_erp.pedido.Pedido;
import com.example.integral_erp.pedido.PedidoRepository;
import com.example.integral_erp.pedidoitem.PedidoItem;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final MovimentacaoService movimentacaoService;
    private final MercadoPagoProperties properties;

    @Transactional
    public Pagamento criarPagamento(Pedido pedido, FormaPagamento formaPagamento) {

        Pagamento pagamento = new Pagamento();

        pagamento.setPedido(pedido);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setGateway(GatewayPagamento.MERCADO_PAGO);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(pedido.getTotal());

        pagamento.setDataExpiracao(LocalDateTime.now().plusMinutes(30));

        return pagamentoRepository.save(pagamento);
    }

    public PixResponseDTO criarPagamentoPix(Pedido pedido) {
        try {
            String url = "https://api.mercadopago.com/v1/payments";
            String accessToken = properties.getAccessToken();

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            // Use apenas o pedido.getId() ou uma lógica que não mude em milissegundos 
            // para que a idempotência realmente funcione se houver um retry.
            headers.set("X-Idempotency-Key", "idemp_pix_pedido_" + pedido.getId());

            // CONSTRUÇÃO DO BODY (Idêntico ao CURL)
            Map<String, Object> body = new HashMap<>();
            body.put("transaction_amount", pedido.getTotal());
            body.put("description", "Pedido #" + pedido.getId());
            body.put("payment_method_id", "pix");
            
            // Payer simples como no seu CURL
            Map<String, Object> payer = new HashMap<>();
            payer.put("email", "email_aleatorio_qualquer@gmail.com"); // Recomendo usar pedido.getCliente().getEmail()
            body.put("payer", payer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            // Fazendo a chamada
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) throw new RuntimeException("Resposta vazia do Mercado Pago");

            // EXTRAÇÃO DOS DADOS (Com segurança para evitar NullPointerException)
            Map<String, Object> poi = (Map<String, Object>) responseBody.get("point_of_interaction");
            Map<String, Object> td = (Map<String, Object>) poi.get("transaction_data");

            String qrCode = (String) td.get("qr_code");
            String qrCodeBase64 = (String) td.get("qr_code_base64");
            String mpId = responseBody.get("id").toString();

            // Salva no banco (Sincronizando com o Gateway)
            Pagamento pagamento = pagamentoRepository.findByPedidoId(pedido.getId())
                    .orElseThrow(() -> new RuntimeException("Pagamento não encontrado no banco"));
            
            pagamento.setTransacaoGatewayId(mpId);
            pagamentoRepository.save(pagamento);

            return new PixResponseDTO(
                    qrCode,
                    qrCodeBase64,
                    pedido.getTotal(),
                    pagamento.getStatus().name(),
                    pagamento.getDataExpiracao()
            );

        } catch (HttpClientErrorException e) {
            // Log detalhado para você ver EXATAMENTE por que o MP recusou
            System.err.println("Erro do Mercado Pago: " + e.getResponseBodyAsString());
            throw new RuntimeException("Mercado Pago recusou o Pix: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Erro interno ao gerar Pix: " + e.getMessage());
            throw new RuntimeException("Erro interno no servidor ao processar pagamento");
        }
    

        // Pagamento pagamento = pagamentoRepository.findByPedidoId(pedido.getId())
        //     .orElseThrow();

        // String accessToken = properties.getAccessToken();

        // MercadoPagoConfig.setAccessToken(accessToken);
     
        // try {
        //     if (accessToken == null || accessToken.isBlank()) {
        //         return new PixResponseDTO(
        //             "PIX-FAKE-CODE",
        //             "BASE64_FAKE",
        //             pedido.getTotal(),
        //             pagamento.getStatus().name(),
        //             pagamento.getDataExpiracao()
        //         );
        //     }

        //     System.out.println("TOKEN: " + accessToken);

        //     //PaymentClient client = new PaymentClient();

        //     Usuario usuario = SecurityUtils.getUsuarioLogado();

        //     PaymentCreateRequest request = 
        //         PaymentCreateRequest.builder()
        //             .transactionAmount(pedido.getTotal().abs())
        //             .description("Pedido #" + pedido.getId())
        //             .paymentMethodId("pix")
        //             .notificationUrl("https://uptake-zoologist-prescribe.ngrok-free.dev/api/pagamentos/webhook")
        //             .payer(
        //                 PaymentPayerRequest.builder()
        //                     .email("test_user_8043693076165900002@testuser.com")
        //                     .identification(
        //                         IdentificationRequest.builder()
        //                         .type("CPF")
        //                         .number("12345678909")
        //                         .build()
        //                     )
        //                     .build()
        //             )
        //             .dateOfExpiration(OffsetDateTime.now().plusMinutes(30))
        //             .build();

        //     //Payment payment = client.create(request);

        //     System.out.println("MP RESPONSE: " + payment);

        //     if (payment.getPointOfInteraction() == null || payment.getPointOfInteraction().getTransactionData() == null) {
        //         throw new RuntimeException("Mercado Pago não retornou dados de PIX");
        //     }

        //     String qrCode = payment.getPointOfInteraction()
        //         .getTransactionData()
        //         .getQrCode();

        //     String qrCodeBase64 = payment.getPointOfInteraction()
        //         .getTransactionData()
        //         .getQrCodeBase64();

        //     pagamento.setTransacaoGatewayId(payment.getId().toString());
        //     pagamentoRepository.save(pagamento);
            
        //     return new PixResponseDTO(qrCode, qrCodeBase64, pedido.getTotal(), pagamento.getStatus().name(), pagamento.getDataExpiracao());
        
        // } catch (MPApiException e) {
        //     System.out.println("STATUS: " + e.getStatusCode());
        //     System.out.println("RESPONSE: " + e.getApiResponse().getContent());
        //     throw new RuntimeException("Erro Mercado Pago", e);

        // } catch (Exception e) {
            
        //     e.printStackTrace();
        //     throw new RuntimeException("Erro ao gerar PIX", e);
        // } 

    }

    @Transactional
    public CartaoPagamentoResponseDTO criarPagamentoCartao (Long pedidoId, CartaoPagamentoRequestDTO dto) {

        String accessToken = properties.getAccessToken();
        MercadoPagoConfig.setAccessToken(accessToken);

        try {
            PaymentClient client = new PaymentClient();

            Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));

            if (StatusPedido.PAGO.equals(pedido.getStatus())) {
                throw new RuntimeException("Este pedido já foi processado e pago");
            }

            PaymentCreateRequest request = 
                PaymentCreateRequest.builder()
                    .transactionAmount(pedido.getTotal())
                    .token(dto.token())
                    .description("Pedido #" + pedido.getId())
                    .installments(dto.installments())
                    .paymentMethodId(dto.paymentMethodId())
                    .payer(
                        PaymentPayerRequest.builder()
                            .email(dto.email())
                            .build())
                    .build();

            Map<String, String> headers = new HashMap<>();
            headers.put("X-Idempotency-Key", pedidoId.toString() + "-" + dto.token().substring(0,5));

            MPRequestOptions options = MPRequestOptions.builder()
                .customHeaders(headers)
                .build();

            Payment payment = client.create(request, options);

            if ("approved".equalsIgnoreCase(payment.getStatus())) {
                
                pedido.setStatus(StatusPedido.PAGO);

                for (PedidoItem item : pedido.getItens()) {
                    movimentacaoService.saidaVenda(item.getProdutoId(), item.getQuantidade());
                }

                pedidoRepository.save(pedido);
            }

            return new CartaoPagamentoResponseDTO(
                payment.getStatus(),
                payment.getStatusDetail()
            );
            
        } catch (MPApiException e) {
            System.err.println("Erro API Mercado Pago:");
            String content = e.getApiResponse().getContent();
            System.err.println("Detalhes: " + content);
            throw new RuntimeException("Erro Mercado Pago: " + content, e);
        
        } catch (MPException e) {
            throw new RuntimeException("Erro geral Mercado Pago", e);
        }
    }

    public PagamentoStatusDTO buscarStatus(Long pedidoId) {

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId)
            .orElseThrow();

        return new PagamentoStatusDTO(
            pagamento.getStatus().name(),
            pagamento.getPedido().getStatus().name()
        );
    }

    @Transactional
    public void aprovarPagamento(Pagamento pagamento) {

        if (pagamento.getStatus() == StatusPagamento.APROVADO) return;

        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataConfirmacao(LocalDateTime.now());

        Pedido pedido = pagamento.getPedido();
        pedido.setStatus(StatusPedido.PAGO);

        for (PedidoItem item : pedido.getItens()) {
            movimentacaoService.saidaVenda(item.getProdutoId(), item.getQuantidade());
        }
    }

    @Transactional
    public void simularPagamento(Long pedidoId) {

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataConfirmacao(LocalDateTime.now());

        Pedido pedido = pagamento.getPedido();
        pedido.setStatus(StatusPedido.PAGO);

        // Baixa estoque
        for (PedidoItem item : pedido.getItens()) {
            movimentacaoService.saidaVenda(item.getProdutoId(), item.getQuantidade());
        }
    }

    @Transactional
    public void confirmarPagamento(String transacaoId) {

        Pagamento pagamento = pagamentoRepository
                .findByTransacaoGatewayId(transacaoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataConfirmacao(LocalDateTime.now());

        Pedido pedido = pagamento.getPedido();
        pedido.setStatus(StatusPedido.PAGO);
    }

    public Pagamento buscarPorPedido(Long pedidoId) {
        return pagamentoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public Pagamento processarPagamentoManual(Pedido pedido, FormaPagamento formaPagamento) {
        Pagamento pagamento = criarPagamento(pedido, formaPagamento);

        pagamento.setGateway(GatewayPagamento.MANUAL);
        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataConfirmacao(LocalDateTime.now());

        return pagamentoRepository.save(pagamento);
    }
}
