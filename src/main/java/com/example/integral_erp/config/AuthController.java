package com.example.integral_erp.config;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integral_erp.config.dto.LoginRequestDTO;
import com.example.integral_erp.config.dto.LoginResponseDTO;
import com.example.integral_erp.config.dto.UsuarioMeResponseDTO;
import com.example.integral_erp.usuario.Usuario;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
        @RequestBody LoginRequestDTO request
    ) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(),
                request.senha()
            )
        );

        UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();

        Usuario usuario = usuarioDetails.getUsuario();

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(token));
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
}
