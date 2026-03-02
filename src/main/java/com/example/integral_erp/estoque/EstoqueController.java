package com.example.integral_erp.estoque;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.estoque.dto.EstoqueAlertaResponse;
import com.example.integral_erp.estoque.dto.EstoqueResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estoques")
@RequiredArgsConstructor
public class EstoqueController {
    
    private final EstoqueService estoqueService;

    @GetMapping
    public ResponseEntity<List<EstoqueResponse>> listarTodos() {
       
        return ResponseEntity.ok(estoqueService.listarTodos());
    }

    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<EstoqueResponse>> listarPorCentro(@PathVariable Long centroId) {

        return ResponseEntity.ok(estoqueService.listarPorCentro(centroId));
    }

    @GetMapping("/centro/{centroId}/produto/{produtoId}")
    public ResponseEntity<EstoqueResponse> buscar(@PathVariable Long centroId, @PathVariable Long produtoId) {

        return ResponseEntity.ok(estoqueService.buscar(centroId, produtoId));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<EstoqueAlertaResponse>> alertas() {
    
        return ResponseEntity.ok(estoqueService.listarAbaixoDoMinimo());
    }
}
