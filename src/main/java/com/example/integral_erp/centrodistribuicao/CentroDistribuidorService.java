package com.example.integral_erp.centrodistribuicao;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.integral_erp.centrodistribuicao.dto.CriarDistribuidorRequestDTO;
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
        centro.setNome(request.nomeCentro());
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
}
