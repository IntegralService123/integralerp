package com.example.integral_erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.centrodistribuicao.CentroDistribuicaoRepository;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.enums.TipoCentro;
import com.example.integral_erp.usuario.Usuario;
import com.example.integral_erp.usuario.UsuarioRepository;

@Component
@Order(2)
public class UsuarioSeed implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CentroDistribuicaoRepository centroRepository;

    @Value("${spring.usuarioadmin.email}")
    private String adminEmail;

    @Value("${spring.usuarioadmin.password}")
    private String adminPassword;

    public UsuarioSeed(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, CentroDistribuicaoRepository centroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.centroRepository = centroRepository;
    }

    @Override
    public void run (String... args) {

        Usuario admin = usuarioRepository.findByEmail(adminEmail).orElse(null);

        CentroDistribuicao centroBase = centroRepository
                .findFirstByTipo(TipoCentro.BASE)
                .orElseThrow(() -> new RuntimeException("Centro BASE não encontrado"));

        if (admin == null) {

            

            Usuario novoAdmin = new Usuario();
            novoAdmin.setNome("Administrador");
            novoAdmin.setEmail(adminEmail.trim());
            novoAdmin.setSenha(passwordEncoder.encode(adminPassword.trim()));
            novoAdmin.setRole(Role.BASE_ADMIN);
            novoAdmin.setCentro(centroBase);

            usuarioRepository.save(novoAdmin);

            System.out.println("Usuário ADMIN criado com sucesso");

        } else if (admin.getCentro() == null) {

            admin.setCentro(centroBase);
            usuarioRepository.save(admin);

            System.out.println("Centro vinculado ao ADMIN existente");
        }
    }
}
