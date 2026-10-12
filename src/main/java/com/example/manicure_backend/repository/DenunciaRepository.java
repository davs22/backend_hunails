package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    void deleteByDenunciante_IdUsuario(Long idUsuario);
    void deleteByUsuarioAlvo_IdUsuario(Long idUsuario);
    void deleteByPostAlvo_IdPostIn(Iterable<Long> postIds);
}
