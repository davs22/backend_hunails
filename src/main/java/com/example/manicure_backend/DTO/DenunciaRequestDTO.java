package com.example.manicure_backend.DTO;

import lombok.Data;

@Data
public class DenunciaRequestDTO {
    private String tipo;
    private Long usuarioAlvoId;
    private Long postAlvoId;
    private String motivo;
    private String detalhes;
}
