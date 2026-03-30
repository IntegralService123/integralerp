package com.example.integral_erp.movimentacaoestoque;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.movimentacaoestoque.dto.EntradaMovimentacaoRequestDTO;
import com.example.integral_erp.movimentacaoestoque.dto.MovimentacaoResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping("/entrada")
    public ResponseEntity<MovimentacaoResponseDTO> entrada(
            @RequestBody EntradaMovimentacaoRequestDTO request) {

        return ResponseEntity.ok(
            movimentacaoService.entrada(request)
        );
    }

    @GetMapping
    public Page<MovimentacaoResponseDTO> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long produtoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return movimentacaoService.listar(tipo, produtoId, page, size);
    }
    
}
