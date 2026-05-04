package com.example.integral_erp.frete;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.frete.dto.FreteRequestDTO;
import com.example.integral_erp.frete.dto.FreteResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/frete")
@RequiredArgsConstructor
public class FreteController {

    private final FreteService freteService;

    @PostMapping("/calcular")
    public ResponseEntity<List<FreteResponseDTO>> calcular (@RequestBody FreteRequestDTO request) {
        return ResponseEntity.ok(freteService.calcularOpcoes(request));
    }
}
