package com.example.integral_erp.usuario;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.centrodistribuicao.CentroDistribuidorService;
import com.example.integral_erp.centrodistribuicao.dto.CriarDistribuidorRequestDTO;
import com.example.integral_erp.usuario.dto.UsuarioResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final CentroDistribuidorService centroService;

    // 🔹 LISTAR USUÁRIOS (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('BASE_ADMIN')")
    public List<UsuarioResponseDTO> listar() {

        return usuarioRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    // 🔹 CRIAR DISTRIBUIDOR
    @PostMapping("/distribuidor")
    @PreAuthorize("hasRole('BASE_ADMIN')")
    public void criarDistribuidor(
            @RequestBody CriarDistribuidorRequestDTO request) {

        centroService.criarCentroDistribuidor(request);
    }

    private UsuarioResponseDTO toResponse(Usuario u) {
        return new UsuarioResponseDTO(
            u.getId(),
            u.getNome(),
            u.getEmail(),
            u.getRole().name(),
            u.getCentro() != null ? u.getCentro().getNome() : null
        );
    }
}
