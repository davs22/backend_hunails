package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Comentario;
import com.example.manicure_backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByPostOrderByDataDesc(Post post);
    void deleteByUsuario_IdUsuario(Long idUsuario);
    void deleteByPost_IdPostIn(Iterable<Long> postIds);
}
