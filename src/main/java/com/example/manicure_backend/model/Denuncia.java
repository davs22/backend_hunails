package com.example.manicure_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Denuncia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_denunciante")
    private Usuario denunciante;

    @ManyToOne
    @JoinColumn(name = "id_usuario_alvo")
    private Usuario usuarioAlvo;

    @ManyToOne
    @JoinColumn(name = "id_post_alvo")
    private Post postAlvo;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 120)
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
