package com.example.integral_erp.produto;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.produto.dto.ProdutoEstoqueResponseDTO;
import com.example.integral_erp.produto.dto.ProdutoRequest;
import com.example.integral_erp.produto.dto.ProdutoResponseAdminDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoResponseAdminDTO> criar(
            @RequestBody ProdutoRequest request) {

        return ResponseEntity.ok(produtoService.criar(request));
    }

    @GetMapping
    public List<ProdutoResponseAdminDTO> listar(
        @RequestParam(required = false) Long categoria,
        @RequestParam(required = false) Boolean incluirInativos
    ) {
        return produtoService.listar(categoria, incluirInativos);
    }

    @GetMapping("/estoque")
    public List<ProdutoEstoqueResponseDTO> listarComEstoque(
        @RequestParam(required = false) Long centroId,
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) Boolean incluirInativos
    ) {
        return produtoService.listarComEstoque(centroId, categoriaId, incluirInativos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseAdminDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ProdutoResponseAdminDTO atualizar(
            @PathVariable Long id,
            @RequestBody ProdutoRequest request) {

        return produtoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('BASE_ADMIN')")
    public void excluirDefinitivo(@PathVariable Long id) {
        produtoService.excluirDefinitivo(id);
    }

    // @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('BASE_ADMIN')")
    // public void desativar(@PathVariable Long id) {
    //     produtoService.desativar(id);
    // }

    @PatchMapping("/{id}/status")
    public void alternarStatus(@PathVariable Long id) {
        produtoService.alternarStatus(id);
    }
}
