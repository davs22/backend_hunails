package com.example.manicure_backend.service;

import com.example.manicure_backend.DTO.AnalyticsEventRequestDTO;
import com.example.manicure_backend.model.AnalyticsEvent;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.AnalyticsEventRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import com.example.manicure_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsEventRepository analyticsEventRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public void registrarEvento(String authHeader, AnalyticsEventRequestDTO dto) {
        if (dto.getNomeEvento() == null || dto.getNomeEvento().isBlank()) {
            throw new IllegalArgumentException("Evento invalido.");
        }

        Usuario usuario = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String email = jwtUtil.extractEmail(authHeader.substring(7));
                usuario = usuarioRepository.findByEmail(email).orElse(null);
            } catch (Exception ignored) {
            }
        }

        analyticsEventRepository.save(
                AnalyticsEvent.builder()
                        .usuario(usuario)
                        .nomeEvento(dto.getNomeEvento().trim())
                        .tela(dto.getTela())
                        .metadata(dto.getMetadata())
                        .build()
        );
    }
}
