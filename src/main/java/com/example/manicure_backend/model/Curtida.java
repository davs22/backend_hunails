package com.example.manicure_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "curtidas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_usuario", "id_post"}) // Um usuário só curte 1x o mesmo post
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curtida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_post")
    private Post post;
}