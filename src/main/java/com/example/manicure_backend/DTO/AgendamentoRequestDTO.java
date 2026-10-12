package com.example.manicure_backend.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AgendamentoRequestDTO {
    // ID da manicure com quem quero agendar
    private Long manicureId;
    
    // O ID do cliente NÃO vem aqui, pegamos do Token para segurança!

    private String descricao;
    
    @JsonFormat(pattern = "dd/MM/yyyy") // Formato brasileiro
    private LocalDate data;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;
}