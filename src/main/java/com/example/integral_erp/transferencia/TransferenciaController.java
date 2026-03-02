package com.example.integral_erp.transferencia;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.transferencia.dto.TransferenciaRequest;
import com.example.integral_erp.transferencia.dto.TransferenciaResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @PostMapping
    public ResponseEntity<Transferencia> criar (@RequestBody TransferenciaRequest request) {

        var transferencia = transferenciaService.criarTransferencia(request);
        return ResponseEntity.ok(transferencia);
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmar (@PathVariable Long id) {

        transferenciaService.confirmarTransferencia(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {

        transferenciaService.cancelarTransferencia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TransferenciaResponse>> listar() {
        return ResponseEntity.ok(transferenciaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferenciaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(transferenciaService.buscarPorId(id));
    }
}
