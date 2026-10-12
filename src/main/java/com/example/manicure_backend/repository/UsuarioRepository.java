package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Query("""
        SELECT DISTINCT u
        FROM Usuario u
        LEFT JOIN u.complemento c
        WHERE (
            :q IS NULL OR :q = '' OR
            LOWER(u.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.especialidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.regiao, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.bairro, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.cidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.estado, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    List<Usuario> searchUsers(@Param("q") String q);

    @Query("""
        SELECT DISTINCT u
        FROM Usuario u
        LEFT JOIN u.complemento c
        WHERE c IS NOT NULL
          AND (
            :q IS NULL OR :q = '' OR
            LOWER(u.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.especialidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.regiao, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.bairro, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.cidade, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.estado, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
          AND (:bairro IS NULL OR :bairro = '' OR LOWER(COALESCE(u.bairro, '')) LIKE LOWER(CONCAT('%', :bairro, '%')))
          AND (:cidade IS NULL OR :cidade = '' OR LOWER(COALESCE(u.cidade, '')) LIKE LOWER(CONCAT('%', :cidade, '%')))
          AND (:estado IS NULL OR :estado = '' OR LOWER(COALESCE(u.estado, '')) LIKE LOWER(CONCAT('%', :estado, '%')))
    """)
    List<Usuario> findFilteredManicures(
            @Param("q") String q,
            @Param("bairro") String bairro,
            @Param("cidade") String cidade,
            @Param("estado") String estado
    );
}
