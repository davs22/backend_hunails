package com.example.manicure_backend.DTO;

import com.example.manicure_backend.model.StatusAgendamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AgendamentoDTO {
    private Long idAgendamento;
    private String descricao;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate data;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime hora;
    private Double valor;
    private StatusAgendamento status;

    private UsuarioDTO usuario;
    private UsuarioDTO manicure;
}
