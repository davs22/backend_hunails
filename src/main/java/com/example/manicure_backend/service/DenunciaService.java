package com.example.manicure_backend.service;

import com.example.manicure_backend.DTO.DenunciaRequestDTO;
import com.example.manicure_backend.model.Denuncia;
import com.example.manicure_backend.model.Post;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.DenunciaRepository;
import com.example.manicure_backend.repository.PostRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import com.example.manicure_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;

    public void denunciar(String authHeader, DenunciaRequestDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new IllegalArgumentException("Tipo de denuncia invalido.");
        }
        if (dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            throw new IllegalArgumentException("Motivo obrigatorio.");
        }

        String email = jwtUtil.extractEmail(authHeader.substring(7));
        Usuario denunciante = usuarioRepository.findByEmail(email).orElseThrow();

        Usuario usuarioAlvo = null;
        Post postAlvo = null;

        if ("USUARIO".equalsIgnoreCase(dto.getTipo())) {
            usuarioAlvo = usuarioRepository.findById(dto.getUsuarioAlvoId()).orElseThrow();
        } else if ("POST".equalsIgnoreCase(dto.getTipo())) {
            postAlvo = postRepository.findById(dto.getPostAlvoId()).orElseThrow();
        } else {
            throw new IllegalArgumentException("Tipo de denuncia nao suportado.");
        }

        denunciaRepository.save(
                Denuncia.builder()
                        .denunciante(denunciante)
                        .usuarioAlvo(usuarioAlvo)
                        .postAlvo(postAlvo)
                        .tipo(dto.getTipo().trim().toUpperCase())
                        .motivo(dto.getMotivo().trim())
                        .detalhes(dto.getDetalhes())
                        .build()
        );
    }
}
