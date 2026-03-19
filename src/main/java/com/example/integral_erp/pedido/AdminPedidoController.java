package com.example.integral_erp.pedido;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.pedido.dto.PedidoResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class AdminPedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscar(@PathVariable Long id) {
        return pedidoService.buscarPorIdAdmin(id);
    }

    @PatchMapping("/{id}/status")
    public PedidoResponseDTO atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return pedidoService.atualizarStatus(id, status);
    }
}
