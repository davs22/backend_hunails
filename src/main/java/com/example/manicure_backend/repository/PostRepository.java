package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByAuthorEmail(String email);
    List<Post> findAllByAuthor_IdUsuario(Long idUsuario);

    @Query("""
        SELECT p
        FROM Post p
        LEFT JOIN p.author a
        LEFT JOIN a.complemento c
        WHERE (
            :q IS NULL OR :q = '' OR
            LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(p.descricao, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.especialidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.regiao, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(a.bairro, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(a.cidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(a.estado, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (:bairro IS NULL OR :bairro = '' OR LOWER(COALESCE(a.bairro, '')) LIKE LOWER(CONCAT('%', :bairro, '%')))
        AND (:cidade IS NULL OR :cidade = '' OR LOWER(COALESCE(a.cidade, '')) LIKE LOWER(CONCAT('%', :cidade, '%')))
        AND (:estado IS NULL OR :estado = '' OR LOWER(COALESCE(a.estado, '')) LIKE LOWER(CONCAT('%', :estado, '%')))
        ORDER BY p.data DESC, p.idPost DESC
    """)
    List<Post> findFilteredPosts(
            @Param("q") String q,
            @Param("bairro") String bairro,
            @Param("cidade") String cidade,
            @Param("estado") String estado
    );

    @Query("""
        SELECT p
        FROM Post p
        LEFT JOIN p.author a
        LEFT JOIN a.complemento c
        WHERE (
            :q IS NULL OR :q = '' OR
            LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(p.descricao, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.especialidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.regiao, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(a.bairro, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(a.cidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(a.estado, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (:bairro IS NULL OR :bairro = '' OR LOWER(COALESCE(a.bairro, '')) LIKE LOWER(CONCAT('%', :bairro, '%')))
        AND (:cidade IS NULL OR :cidade = '' OR LOWER(COALESCE(a.cidade, '')) LIKE LOWER(CONCAT('%', :cidade, '%')))
        AND (:estado IS NULL OR :estado = '' OR LOWER(COALESCE(a.estado, '')) LIKE LOWER(CONCAT('%', :estado, '%')))
    """)
    Page<Post> findFilteredPostsPage(
            @Param("q") String q,
            @Param("bairro") String bairro,
            @Param("cidade") String cidade,
            @Param("estado") String estado,
            Pageable pageable
    );
}
