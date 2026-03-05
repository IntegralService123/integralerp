package com.example.integral_erp.centrodistribuicao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.centrodistribuicao.dto.CriarDistribuidorRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/distribuidores")
@RequiredArgsConstructor
public class CentroDistribuidorController {

    private final CentroDistribuidorService distribuidorService;

    @PostMapping
    @PreAuthorize("hasRole('BASE_ADMIN')")
    public ResponseEntity<Void> criar(
            @RequestBody CriarDistribuidorRequestDTO request) {

        distribuidorService.criarCentroDistribuidor(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
