package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByManicure_IdUsuario(Long idUsuario);

    List<Agendamento> findByUsuario_IdUsuario(Long idUsuario);

    boolean existsByManicure_IdUsuarioAndDataAndHora(
            Long manicureId,
            LocalDate data,
            LocalTime hora
    );

    void deleteByUsuario_IdUsuario(Long idUsuario);
    void deleteByManicure_IdUsuario(Long idUsuario);
}


