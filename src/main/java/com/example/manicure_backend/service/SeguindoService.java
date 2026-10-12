package com.example.manicure_backend.service;

import com.example.manicure_backend.model.Seguindo;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.SeguindoRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class SeguindoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguindoRepository seguindoRepository;

    @Transactional
    public void follow(Long seguidorId, Long seguidoId) {
        if (seguidorId.equals(seguidoId)) {
            throw new IllegalStateException("Um usuario nao pode seguir a si mesmo.");
        }

        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new NoSuchElementException("Usuario seguido nao encontrado."));

        if (seguido.getComplemento() == null) {
            throw new IllegalStateException("Apenas perfis de manicure podem ser seguidos.");
        }

        if (seguindoRepository.existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(seguidorId, seguidoId)) {
            throw new IllegalStateException("Voce ja esta seguindo este usuario.");
        }

        Usuario seguidorReference = usuarioRepository.getReferenceById(seguidorId);

        Seguindo seguindo = Seguindo.builder()
                .seguidor(seguidorReference)
                .seguido(seguido)
                .build();

        seguindoRepository.save(seguindo);
    }

    @Transactional
    public void unfollow(Long seguidorId, Long seguidoId) {
        Seguindo seguimento = seguindoRepository.findBySeguidor_IdUsuarioAndSeguido_IdUsuario(seguidorId, seguidoId)
                .orElseThrow(() -> new NoSuchElementException("Voce nao esta seguindo este usuario."));

        seguindoRepository.delete(seguimento);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long seguidorId, Long seguidoId) {
        return seguindoRepository.existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(seguidorId, seguidoId);
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(Long usuarioId) {
        return seguindoRepository.countBySeguido_IdUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long usuarioId) {
        return seguindoRepository.countBySeguidor_IdUsuario(usuarioId);
    }
}
