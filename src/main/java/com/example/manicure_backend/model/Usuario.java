package com.example.manicure_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importante trocar para ManagedReference se der erro de loop
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    // 🔴 MUDANÇA AQUI: De LAZY para EAGER
    // Isso garante que os dados de manicure (especialidade/regiao) venham sempre.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference // Ajuda a serializar corretamente o complemento
    private Complementos complemento;

    @Column(nullable = false)
    private String nome;

    private Integer idade;

    @Column(nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private String senha;

    @Column(nullable = false, unique = true)
    private String email;

    // ANTES: @Column(nullable = true, name = "url_foto_perfil")
    // DEPOIS: Aumenta o limite para aceitar a string Base64 completa
    @Column(nullable = true, name = "url_foto_perfil", columnDefinition = "TEXT")
    private String urlFotoPerfil;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Column(name = "cep")
    private String cep;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "estado")
    private String estado;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Sexo sexo = Sexo.F;
}
