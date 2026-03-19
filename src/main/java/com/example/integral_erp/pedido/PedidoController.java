package com.example.integral_erp.pedido;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.pedido.dto.PedidoRequestDTO;
import com.example.integral_erp.pedido.dto.PedidoResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public PedidoResponseDTO criar(@RequestBody PedidoRequestDTO request) {
        return pedidoService.criar(request);
    }

    @GetMapping
    public List<PedidoResponseDTO> listar() {
        return pedidoService.listarMeusPedidos();
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscar(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }
}
