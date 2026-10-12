package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Complementos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComplementosRepository extends JpaRepository<Complementos, Long> {

    Optional<Complementos> findByUsuario_IdUsuario(Long idUsuario);
}
