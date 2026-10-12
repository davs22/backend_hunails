package com.example.manicure_backend.DTO;

import com.example.manicure_backend.model.Sexo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {
    private Long idUsuario;
    private String nome;
    private Integer idade;
    private String email;
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

    // Dados de Manicure
    private String especialidade;
    private String regiao;

    @JsonProperty("isManicure") // 🚀 Força o nome exato que o Flutter envia
    private boolean isManicure;

    // Dados Sociais (Novos)
    private long seguidores;
    private long seguindo;
    private boolean seguidoPorMim;
}
