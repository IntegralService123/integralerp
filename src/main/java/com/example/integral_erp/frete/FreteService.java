package com.example.integral_erp.frete;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.integral_erp.carrinho.Carrinho;
import com.example.integral_erp.carrinho.CarrinhoRepository;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.frete.dto.FreteRequestDTO;
import com.example.integral_erp.frete.dto.FreteResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FreteService {

    private final CarrinhoRepository carrinhoRepository;
    private final RestTemplate restTemplate;

    private final String URL_MELHOR_ENVIO = "https://sandbox.melhorenvio.com.br/api/v2/me/shipment/calculate";
    @Value("${spring.melhorenvio.token}") private String token;

    public List<FreteResponseDTO> calcularOpcoes(FreteRequestDTO request) {

        Long usuarioId = SecurityUtils.getUsuarioId();

        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", Map.of("postal_code", "25251600"));
        requestBody.put("to", Map.of("postal_code", request.cepDestino()));

        List<Map<String, Object>> produtos = carrinho.getItens().stream().map(item -> {
            var p = item.getProduto();
            Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", p.getId().toString());
                productMap.put("width", p.getLargura());
                productMap.put("height", p.getAltura());
                productMap.put("length", p.getComprimento());
                productMap.put("weight", p.getPeso());
                productMap.put("insurance_value", p.getPreco());
                productMap.put("quantity", item.getQuantidade());
                return productMap;
        }).toList();

        requestBody.put("products", produtos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("Accept", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<List> response = restTemplate.postForEntity(URL_MELHOR_ENVIO, entity, List.class);

            return formatarResposta(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar frete: " + e.getMessage());
        }
    }

    private List<FreteResponseDTO> formatarResposta(List<Map<String, Object>> corpo) {

        return corpo.stream()
            .filter(op -> op.get("error") == null)
            .map(op -> {
                String nome = (String) op.get("name");
                BigDecimal valor = new BigDecimal(op.get("price").toString());
                Integer prazo = (Integer) op.get("delivery_time");

                Map<String, Object> company = (Map<String, Object>) op.get("company");
                String empresa = (String) company.get("name");

                return new FreteResponseDTO(nome, valor, prazo, empresa);
            }).toList();
    }
}
