package com.example.integral_erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.integral_erp.enums.Role;
import com.example.integral_erp.usuario.Usuario;
import com.example.integral_erp.usuario.UsuarioRepository;

@Component
public class UsuarioSeed implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.usuarioadmin.email}")
    private String adminEmail;

    @Value("${spring.usuarioadmin.password}")
    private String adminPassword;

    public UsuarioSeed(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run (String... args) {
        if (usuarioRepository.count() == 0) {

            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail(adminEmail.trim());
            admin.setSenha(passwordEncoder.encode(adminPassword.trim()));
            admin.setRole(Role.BASE_ADMIN);

            usuarioRepository.save(admin);

            System.out.println("Usuário ADMIN criado com sucesso");
        }
    }
}
