package com.example.manicure_backend.DTO;

import lombok.Data;

@Data
public class AnalyticsEventRequestDTO {
    private String nomeEvento;
    private String tela;
    private String metadata;
}
