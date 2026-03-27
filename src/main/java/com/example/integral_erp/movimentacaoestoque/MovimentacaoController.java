package com.example.integral_erp.movimentacaoestoque;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<MovimentacaoResponseDTO> listar() {
        return movimentacaoService.listar();
    }
    
}
