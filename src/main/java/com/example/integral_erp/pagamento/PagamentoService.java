package com.example.integral_erp.pagamento;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
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
import com.example.integral_erp.pagamento.dto.BoletoRequestDTO;
import com.example.integral_erp.pagamento.dto.BoletoResponseDTO;
import com.example.integral_erp.pagamento.dto.CartaoPagamentoRequestDTO;
import com.example.integral_erp.pagamento.dto.CartaoPagamentoResponseDTO;
import com.example.integral_erp.pagamento.dto.PagamentoStatusDTO;
import com.example.integral_erp.pagamento.dto.PixResponseDTO;
import com.example.integral_erp.pedido.Pedido;
import com.example.integral_erp.pedido.PedidoRepository;
import com.example.integral_erp.pedidoitem.PedidoItem;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
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

    public PixResponseDTO criarPagamentoPix(Pedido pedido, String nomeCompletoPagador, String emailPagador, String cpfPagador) {

        try {

            System.out.println("======= INICIANDO PAGAMENTO PIX =======");
            System.out.println("ID Pedido: " + pedido.getId());

            if (pedido.getUsuario() == null) {
                System.err.println("ERRO: Usuário do pedido está NULL!");
            } else {
                System.out.println("Email do Usuário: " + pedido.getUsuario().getEmail());
            }
            
            System.out.println("Valor Total: " + pedido.getTotal());

            Optional<Pagamento> pagamentoOpt = pagamentoRepository.findByPedidoId(pedido.getId());

            if (pagamentoOpt.isPresent()) {
                Pagamento pag = pagamentoOpt.get();
                
                // Verificamos se é PIX, está PENDENTE e se o QR Code ainda não expirou
                if (pag.getFormaPagamento() == FormaPagamento.PIX && 
                    pag.getStatus() == StatusPagamento.PENDENTE &&
                    pag.getDataExpiracao() != null && 
                    pag.getDataExpiracao().isAfter(LocalDateTime.now().plusSeconds(10))) {
                    
                    // Se o QR Code já estiver salvo no banco, retorna ele direto
                    if (pag.getQrCode() != null) {
                        return new PixResponseDTO(
                            pag.getQrCode(),
                            pag.getQrCodeBase64(),
                            pag.getValor(),
                            pag.getStatus().name(),
                            pag.getDataExpiracao()
                        );
                    }
                }
            }

            String url = "https://api.mercadopago.com/v1/payments";
            String accessToken = properties.getAccessToken();
            System.out.println("DEBUG TOKEN: " + accessToken);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            headers.set("X-Idempotency-Key", "pix_" + pedido.getId() + "_" + System.currentTimeMillis());

            OffsetDateTime expiracao = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).plusMinutes(30);
            String dateExpiration = expiracao.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

            Map<String, Object> body = new HashMap<>();
            body.put("transaction_amount", pedido.getTotal());
            body.put("description", "Pedido #" + pedido.getId());
            body.put("payment_method_id", "pix");
            //body.put("date_of_expiration", dateExpiration);

            // Map<String, Object> payer = new HashMap<>();
            // // Force e-mail minúsculo e sem espaços
            // payer.put("email", "test_user_123@testuser.com"); 
            // payer.put("first_name", "Joao"); // Evite acentos por segurança
            // payer.put("last_name", "Silva");

            // Map<String, Object> identification = new HashMap<>();
            // identification.put("type", "CPF");
            // identification.put("number", "19119119100"); // CPF padrão de teste
            // payer.put("identification", identification);

            Map<String, Object> payer = new HashMap<>();
            payer.put("email", (emailPagador != null && !emailPagador.isBlank()) 
                    ? emailPagador : pedido.getUsuario().getEmail());

            String nome = "Cliente";
            String sobrenome = "Gr Tools";

            if (nomeCompletoPagador != null && !nomeCompletoPagador.trim().isEmpty()) {
                String nomeLimpo = nomeCompletoPagador.trim();
                int primeiroEspaco = nomeLimpo.indexOf(" ");
                
                if (primeiroEspaco != -1) {
                    nome = nomeLimpo.substring(0, primeiroEspaco);
                    sobrenome = nomeLimpo.substring(primeiroEspaco).trim();
                } else {
                    nome = nomeLimpo; // Se digitou só uma palavra, mantém o sobrenome padrão anterior
                }
            } else if (pedido.getClienteNome() != null) {
                // Fallback usando o nome que está atrelado ao pedido
                String nomeCliente = pedido.getClienteNome().trim();
                int primeiroEspaco = nomeCliente.indexOf(" ");
                if (primeiroEspaco != -1) {
                    nome = nomeCliente.substring(0, primeiroEspaco);
                    sobrenome = nomeCliente.substring(primeiroEspaco).trim();
                } else {
                    nome = nomeCliente;
                }
            }

            payer.put("first_name", nome);
            payer.put("last_name", sobrenome);

            Map<String, Object> identification = new HashMap<>();

            // Lógica para detectar se é CPF ou CNPJ (básico: por tamanho)
            String cleanCpf = cpfPagador.replaceAll("\\D", "");
            identification.put("type", cleanCpf.length() > 11 ? "CNPJ" : "CPF");
            identification.put("number", cleanCpf);
            
            payer.put("identification", identification);

            body.put("payer", payer);

            System.out.println("JSON Body enviado ao MP: " + body.toString());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            System.out.println("Resposta MP Status: " + response.getStatusCode());
            Map<String, Object> responseBody = response.getBody();

            // EXTRAÇÃO DOS DADOS
            Map<String, Object> poi = (Map<String, Object>) responseBody.get("point_of_interaction");
            Map<String, Object> td = (Map<String, Object>) poi.get("transaction_data");
            String qrCode = (String) td.get("qr_code");
            String qrCodeBase64 = (String) td.get("qr_code_base64");
            String mpId = responseBody.get("id").toString();

            // Salva no banco (Sincronizando com o Gateway)
            Pagamento pagamento = pagamentoOpt.orElseGet(() -> {
                Pagamento newPag = new Pagamento();
                newPag.setPedido(pedido);
                newPag.setFormaPagamento(FormaPagamento.PIX);
                newPag.setGateway(GatewayPagamento.MERCADO_PAGO);
                return newPag;
            });

            pagamento.setStatus(StatusPagamento.PENDENTE);
            pagamento.setValor(pedido.getTotal());
            pagamento.setTransacaoGatewayId(mpId);
            pagamento.setQrCode(qrCode);
            pagamento.setQrCodeBase64(qrCodeBase64);
            pagamento.setDataExpiracao(expiracao.toLocalDateTime()); // Salva os 30 min exatos do MP
            
            pagamentoRepository.save(pagamento);

            return new PixResponseDTO(
                qrCode,
                qrCodeBase64,
                pedido.getTotal(),
                pagamento.getStatus().name(),
                pagamento.getDataExpiracao()
            );

        } catch (HttpServerErrorException e) {
            // Captura especificamente o erro 500 do Mercado Pago
            System.err.println("ERRO 500 NO MP: " + e.getResponseBodyAsString());
            throw new RuntimeException("Mercado Pago está instável. Tente novamente em instantes.");
        } catch (Exception e) {
            System.err.println("Erro Geral: " + e.getMessage());
            throw new RuntimeException("Erro ao processar pagamento Pix.");
        }
    }

    @Transactional
    public CartaoPagamentoResponseDTO criarPagamentoCartao(Long pedidoId, CartaoPagamentoRequestDTO dto) {

        String accessToken = properties.getAccessToken();
        MercadoPagoConfig.setAccessToken(accessToken);

        try {
            PaymentClient client = new PaymentClient();

            Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));

            if (StatusPedido.PAGO.equals(pedido.getStatus())) {
                throw new RuntimeException("Este pedido já foi processado e pago");
            }

            System.out.println("======= DEBUG SERVICE (CARTÃO) =======");
            System.out.println("Token: " + (dto.token() != null ? dto.token().substring(0, 10) + "..." : "NULL"));
            System.out.println("Payment Method: " + dto.paymentMethodId());
            System.out.println("Installments: " + dto.installments());
            System.out.println("Email Payer: " + dto.email());

            String documentoLimpo = dto.cpf() != null ? dto.cpf().replaceAll("\\D", "") : "";

            if (documentoLimpo.isEmpty()) {
                throw new RuntimeException("O CPF ou CNPJ do pagador é obrigatório para processar o pagamento.");
            }

            String tipoDocumento = documentoLimpo.length() > 11 ? "CNPJ" : "CPF";

            // 1. Configuração da Requisição
            PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(pedido.getTotal())
                .token(dto.token())
                .description("Pedido #" + pedido.getId())
                .installments(dto.installments())
                .paymentMethodId(dto.paymentMethodId())
                .payer(PaymentPayerRequest.builder()
                    .email(dto.email())
                    .identification(IdentificationRequest.builder()
                        .type(tipoDocumento)
                        .number(documentoLimpo) // CPF de teste padrão MP
                        .build())
                    .build())
                .build();

            String idempotencyKey = UUID.randomUUID().toString();
            System.out.println("Chave de Idempotência: " + idempotencyKey);

            Map<String, String> headers = new HashMap<>();
            //headers.put("X-Idempotency-Key", pedidoId.toString() + "-" + dto.token().substring(0, 5));
            headers.put("X-Idempotency-Key", idempotencyKey);

            MPRequestOptions options = MPRequestOptions.builder().customHeaders(headers).build();

            // 2. Chamada ao Gateway
            Payment payment = client.create(request, options);
            String mercadoPagopId = payment.getId().toString();

            // 3. PERSISTÊNCIA DA ENTIDADE PAGAMENTO (O que estava faltando)
            Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId)
                .orElse(new Pagamento());
            
            pagamento.setPedido(pedido);
            pagamento.setFormaPagamento(FormaPagamento.CARTAO_CREDITO);
            pagamento.setGateway(GatewayPagamento.MERCADO_PAGO);
            pagamento.setValor(pedido.getTotal());
            pagamento.setTransacaoGatewayId(mercadoPagopId); // Agora o Webhook vai encontrar este ID

            // 4. Lógica de Aprovação Imediata ou Pendência
            if ("approved".equalsIgnoreCase(payment.getStatus())) {
                // Se o cartão aprovou na hora, usa o método completo (estoque + status)
                aprovarPagamentoCompleto(pagamento);
            } else {
                // Se cair em análise (in_process) ou rejeitado, salva o status mas não baixa estoque
                pagamento.setStatus(StatusPagamento.PENDENTE); 
                pagamentoRepository.save(pagamento);
                System.out.println("PAGAMENTO VIA CARTÃO AGUARDANDO: ID " + mercadoPagopId);
            }

            return new CartaoPagamentoResponseDTO(
                payment.getStatus(),
                payment.getStatusDetail()
            );

        } catch (MPApiException e) {
            String content = e.getApiResponse().getContent();
            throw new RuntimeException("Erro API Mercado Pago: " + content, e);
        } catch (MPException e) {
            throw new RuntimeException("Erro geral Mercado Pago", e);
        }
    }

    @Transactional
    public BoletoResponseDTO criarPagamentoBoleto(Pedido pedido, BoletoRequestDTO request) {
        try {
            Optional<Pagamento> pagamentoOpt = pagamentoRepository.findByPedidoId(pedido.getId());

            if (pagamentoOpt.isPresent()) {
                Pagamento pag = pagamentoOpt.get();
                if (StatusPagamento.PENDENTE.equals(pag.getStatus()) &&
                    pag.getDataExpiracao() != null &&
                    pag.getDataExpiracao().isAfter(LocalDateTime.now()) &&
                    pag.getLinhaDigitavel() != null) {

                    return new BoletoResponseDTO(
                        pag.getPayload(),
                        pag.getLinhaDigitavel(),
                        pag.getUrlPagamento(),
                        pag.getValor(),
                        pag.getStatus().name(),
                        pag.getDataExpiracao()
                    );
                }
            }

            String url = "https://api.mercadopago.com/v1/payments";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getAccessToken());
            headers.set("X-Idempotency-Key", "bol_" + pedido.getId() + "_" + System.currentTimeMillis());

            Map<String, Object> body = new HashMap<>();
            body.put("transaction_amount", pedido.getTotal());
            body.put("description", "Pedido #" + pedido.getId());
            body.put("payment_method_id", "bolbradesco");

            Map<String, Object> payer = new HashMap<>();
            payer.put("email", (request.email() != null && !request.email().isBlank()) 
                ? request.email() : pedido.getUsuario().getEmail());
            
            String nome = "Cliente";
            String sobrenome = "Gr Tools";

            if (request.nomePagador() != null && !request.nomePagador().trim().isEmpty()) {
                String nomeLimpo = request.nomePagador().trim();
                int primeiroEspaco = nomeLimpo.indexOf(" ");
                
                if (primeiroEspaco != -1) {
                    nome = nomeLimpo.substring(0, primeiroEspaco);
                    sobrenome = nomeLimpo.substring(primeiroEspaco).trim();
                } else {
                    nome = nomeLimpo;
                }
            }

            payer.put("first_name", nome);
            payer.put("last_name", sobrenome);

            Map<String, Object> identification = new HashMap<>();
            String documentoLimpo = request.cpfCnpj().replaceAll("\\D", "");
            identification.put("type", documentoLimpo.length() > 11 ? "CNPJ" : "CPF");
            identification.put("number", documentoLimpo);
            payer.put("identification", identification);

            Map<String, Object> address = buscarEnderecoPorCep(request.zipCode());
            payer.put("address", address);

            body.put("payer", payer);

            // Define o vencimento para 3 dias a partir de agora
            OffsetDateTime vencimento = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).plusDays(3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String dateExpiration = vencimento.format(formatter);
            
            body.put("date_of_expiration", dateExpiration);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = new RestTemplate().postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            Map<String, Object> transactionDetails = (Map<String, Object>) responseBody.get("transaction_details");

            String barcode = "";
            if (responseBody.containsKey("barcode")) {
                barcode = (String) ((Map<String, Object>) responseBody.get("barcode")).get("content");
            }
            String digitableLine = (String) transactionDetails.get("digitable_line");
            String pdfUrl = (String) transactionDetails.get("external_resource_url");
            String mercadoPagoId = responseBody.get("id").toString();

            Pagamento pagamento = pagamentoOpt.orElse(new Pagamento());
            pagamento.setPedido(pedido);
            pagamento.setValor(pedido.getTotal());
            pagamento.setFormaPagamento(FormaPagamento.BOLETO);
            pagamento.setGateway(GatewayPagamento.MERCADO_PAGO);
            pagamento.setStatus(StatusPagamento.PENDENTE);

            pagamento.setTransacaoGatewayId(mercadoPagoId);
            pagamento.setLinhaDigitavel(digitableLine);

            pagamento.setPayload(barcode);
            pagamento.setUrlPagamento(pdfUrl);

            pagamento.setDataExpiracao(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).plusDays(3).toLocalDateTime());

            pagamentoRepository.save(pagamento);

            return new BoletoResponseDTO(
                barcode,
                digitableLine,
                pdfUrl,
                pedido.getTotal(),
                "PENDENTE",
                pagamento.getDataExpiracao()
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar boleto: " + e.getMessage());
        }
    }

    private Map<String, Object> buscarEnderecoPorCep(String cep) {
        Map<String, Object> address = new HashMap<>();
        try {
            String url = "https://viacep.com.br/ws/" + cep.replaceAll("\\D", "") + "/json/";
            ResponseEntity<Map> response = new RestTemplate().getForEntity(url, Map.class);
            Map<String, String> data = response.getBody();

            if (data != null && !data.containsKey("erro")) {
                address.put("zip_code", cep.replaceAll("\\D", ""));
                address.put("street_name", data.get("logradouro"));
                address.put("street_number", "000"); // Mercado Pago exige número. Pode mandar '000' ou coletar do front se preferir.
                address.put("neighborhood", data.get("bairro"));
                address.put("city", data.get("localidade"));
                address.put("federal_unit", data.get("uf"));
                return address;
            }
        } catch (Exception e) {
            System.err.println("Falha ao buscar CEP dinâmico, usando fallback padrão: " + e.getMessage());
        }

        // Fallback de contingência caso a API externa falhe
        address.put("zip_code", "04571020");
        address.put("street_name", "Avenida das Nações Unidas");
        address.put("street_number", "3003");
        address.put("neighborhood", "Bonfim");
        address.put("city", "Osasco");
        address.put("federal_unit", "SP");
        return address;
    }

    public PagamentoStatusDTO buscarStatus(Long pedidoId) {

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (pagamento.getStatus() == StatusPagamento.PENDENTE) {
            try {
                PaymentClient client = new PaymentClient();
                Payment mpPayment = client.get(Long.parseLong(pagamento.getTransacaoGatewayId()));

                String mpStatus = mpPayment.getStatus();

                if ("approved".equalsIgnoreCase(mpStatus)) {
                    pagamento.setStatus(StatusPagamento.APROVADO);
                    pagamento.getPedido().setStatus(StatusPedido.PAGO);
                    pagamentoRepository.save(pagamento);
                }
                else if ("cancelled".equalsIgnoreCase(mpStatus) || "expired".equalsIgnoreCase(mpStatus)) {
                    pagamento.setStatus(StatusPagamento.CANCELADO);
                    pagamento.getPedido().setStatus(StatusPedido.CANCELADO);
                    pagamentoRepository.save(pagamento);
                }
            } catch (Exception e) {
                System.err.println("Erro ao consultar Mercado Pago no polling: " + e.getMessage());
            }
        }

        return new PagamentoStatusDTO(
            pagamento.getStatus().name(),
            pagamento.getPedido().getStatus().name()
        );
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

    @Transactional
    public void processarNotificacaoGateway(String transacaoId) {

        Pagamento pagamento = pagamentoRepository
                .findByTransacaoGatewayId(transacaoId)
                .orElseThrow(() -> new RuntimeException("Pagamento " + transacaoId + " não encontrado no banco local."));

        if (StatusPagamento.APROVADO.equals(pagamento.getStatus())) {
            return;
        }

        aprovarPagamentoCompleto(pagamento);
    }

    @Transactional
    public void aprovarPagamentoCompleto(Pagamento pagamento) {

        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataConfirmacao(LocalDateTime.now());

        Pedido pedido = pagamento.getPedido();
        pedido.setStatus(StatusPedido.PAGO);

        for (PedidoItem item : pedido.getItens()) {
            movimentacaoService.saidaVenda(item.getProdutoId(), item.getQuantidade());
        }

        pagamentoRepository.save(pagamento);
        System.out.println("PAGAMENTO APROVADO E ESTOQUE ATUALIZADO: Pedido # " + pedido.getId());
    }
}
