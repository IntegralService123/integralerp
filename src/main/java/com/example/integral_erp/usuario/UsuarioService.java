package com.example.integral_erp.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.integral_erp.enums.Role;
import com.example.integral_erp.usuario.dto.RegisterRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public void registrarCliente(RegisterRequestDTO request) {

        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        // 🔥 REGRA DE NEGÓCIO
        usuario.setRole(Role.CLIENTE);

        usuarioRepository.save(usuario);
    }
}
