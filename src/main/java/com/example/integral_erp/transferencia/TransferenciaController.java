package com.example.integral_erp.transferencia;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.transferencia.dto.ConfirmarTransferenciaRequestDTO;
import com.example.integral_erp.transferencia.dto.TransferenciaRequestDTO;
import com.example.integral_erp.transferencia.dto.TransferenciaResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @PostMapping
    public ResponseEntity<TransferenciaResponseDTO> criar(
            @RequestBody TransferenciaRequestDTO request) {

        return ResponseEntity.ok(transferenciaService.criar(request));
    }

    @PostMapping("/{id}/enviar")
    public void enviar (@PathVariable Long id) {
        transferenciaService.enviar(id);
    }

    @PostMapping("/confirmar")
    public ResponseEntity<TransferenciaResponseDTO> confirmar(
            @RequestBody ConfirmarTransferenciaRequestDTO request) {

        return ResponseEntity.ok(
            transferenciaService.confirmar(request)
        );
    }

    @GetMapping
    public List<TransferenciaResponseDTO> listar() {
        return transferenciaService.listar();
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<TransferenciaResponseDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
            transferenciaService.buscarPorCodigo(codigo)
        );
    }
}
