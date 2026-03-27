package com.example.integral_erp.centrodistribuicao;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.dto.CentroResponseDTO;
import com.example.integral_erp.centrodistribuicao.dto.CriarDistribuidorRequestDTO;
import com.example.integral_erp.config.SecurityUtils;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.enums.TipoCentro;
import com.example.integral_erp.usuario.Usuario;
import com.example.integral_erp.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CentroDistribuidorService {

    private final UsuarioRepository usuarioRepository;
    private final CentroDistribuicaoRepository centroRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void criarCentroDistribuidor(CriarDistribuidorRequestDTO request) {

        CentroDistribuicao centro = new CentroDistribuicao();
        centro.setNome(request.nomeDistribuidor());
        centro.setTipo(TipoCentro.DISTRIBUIDOR);

        centroRepository.save(centro);

        Usuario usuario = new Usuario();
        usuario.setNome(request.nomeDistribuidor());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(Role.DISTRIBUIDOR);
        usuario.setCentro(centro);

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<CentroResponseDTO> listarCentros() {

        Usuario usuario = SecurityUtils.getUsuarioLogado();

        // ADMIN vê todos
        if (usuario.getRole() == Role.BASE_ADMIN) {
            return centroRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        }

        // DISTRIBUIDOR vê só o próprio
        return List.of(toResponse(usuario.getCentro()));
    }

    private CentroResponseDTO toResponse(CentroDistribuicao centro) {
        return new CentroResponseDTO(
            centro.getId(),
            centro.getNome(),
            centro.getTipo().name()
        );
    }
}
