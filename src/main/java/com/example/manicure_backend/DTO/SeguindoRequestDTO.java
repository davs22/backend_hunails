package com.example.manicure_backend.DTO;
public class SeguindoRequestDTO {
    
    private Long seguidoId;

    // 1. Construtor Vazio (OBRIGATÓRIO para o JSON funcionar)
    public SeguindoRequestDTO() {}

    // 2. Construtor com argumentos
    public SeguindoRequestDTO(Long seguidoId) {
        this.seguidoId = seguidoId;
    }

    // 3. Getters e Setters
    public Long getSeguidoId() {
        return seguidoId;
    }

    public void setSeguidoId(Long seguidoId) {
        this.seguidoId = seguidoId;
    }
}