package com.example.manicure_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguindo", uniqueConstraints = {
    // Garante que um usuário não possa seguir o mesmo alvo duas vezes
    @UniqueConstraint(columnNames = {"seguidor_id", "seguido_id"}) 
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguindo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuário que está realizando a ação de seguir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguidor_id", nullable = false)
    private Usuario seguidor;

    // Usuário que está sendo seguido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguido_id", nullable = false)
    private Usuario seguido;

    @Column(name = "data_seguimento", nullable = false)
    @Builder.Default
    private LocalDateTime dataSeguimento = LocalDateTime.now();
}