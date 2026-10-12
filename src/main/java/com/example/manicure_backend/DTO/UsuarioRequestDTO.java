package com.example.manicure_backend.DTO;

import com.example.manicure_backend.model.Sexo;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    private String nome;
    private Integer idade;
    private String email;
    private String senha;
    private String urlFotoPerfil;
    private String telefone;
    private String whatsapp;
    private String cep;
    private String bairro;
    private String cidade;
    private String estado;
    private String biografia;
    private Double latitude;
    private Double longitude;
    private Sexo sexo;
    private String especialidade;
    private String regiao;
}
