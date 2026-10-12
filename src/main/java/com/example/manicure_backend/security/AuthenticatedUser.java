package com.example.manicure_backend.security;

import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final UsuarioRepository usuarios;

    public Usuario require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public Long optionalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String email)) {
            return null;
        }
        return usuarios.findByEmail(email).map(Usuario::getIdUsuario).orElse(null);
    }

    public void requireOwner(Long id) {
        Usuario current = require();
        if (!usuarios.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!current.getIdUsuario().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
