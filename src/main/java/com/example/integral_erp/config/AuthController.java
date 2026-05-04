package com.example.integral_erp.config;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.config.dto.GoogleLoginRequestDTO;
import com.example.integral_erp.config.dto.LoginRequestDTO;
import com.example.integral_erp.config.dto.LoginResponseDTO;
import com.example.integral_erp.config.dto.UsuarioMeResponseDTO;
import com.example.integral_erp.enums.Role;
import com.example.integral_erp.usuario.Usuario;
import com.example.integral_erp.usuario.UsuarioRepository;
import com.example.integral_erp.usuario.UsuarioService;
import com.example.integral_erp.usuario.dto.RegisterRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(),
                request.senha()
            )
        );

        UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();

        Usuario usuario = usuarioDetails.getUsuario();

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole().name()));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioMeResponseDTO> me(@AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Usuario usuario = usuarioDetails.getUsuario();

        UsuarioMeResponseDTO response = new UsuarioMeResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getRole().name(),
            usuario.getCentro() != null ? usuario.getCentro().getId() : null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequestDTO request) {
        usuarioService.registrarCliente(request);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> googleLogin(@RequestBody GoogleLoginRequestDTO request) {

        Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseGet(() -> {

                Usuario novo = new Usuario();
                novo.setEmail(request.email());
                novo.setNome(request.nome());
                novo.setRole(Role.CLIENTE);
                novo.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
                return usuarioRepository.save(novo);
            });

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(
            token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole().name()
        ));
    }
}
