package com.example.integral_erp.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.integral_erp.enums.Role;
import com.example.integral_erp.usuario.Usuario;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuario();
        }

        return null;
    }

    public static Long getUsuarioId() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getId() : null;
    }

    public static Long getCentroId() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null && usuario.getCentro() != null
                ? usuario.getCentro().getId()
                : null;
    }

    public static Role getRole() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getRole() : null;
    }
}
