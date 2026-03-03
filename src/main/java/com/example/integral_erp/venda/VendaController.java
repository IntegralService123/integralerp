package com.example.integral_erp.venda;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.venda.dto.VendaDetalhadaResponse;
import com.example.integral_erp.venda.dto.VendaRequest;
import com.example.integral_erp.venda.dto.VendaResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @PostMapping
    public ResponseEntity<VendaResponse> criar(
            @RequestBody VendaRequest request) {

        return ResponseEntity.ok(vendaService.criar(request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {

        vendaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/faturar")
    public ResponseEntity<Void> faturar(@PathVariable Long id) {

        vendaService.faturar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<VendaDetalhadaResponse>> listar() {
        return ResponseEntity.ok(vendaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaDetalhadaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }
}
