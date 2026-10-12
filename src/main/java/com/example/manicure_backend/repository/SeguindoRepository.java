package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.Seguindo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeguindoRepository extends JpaRepository<Seguindo, Long> {
    
    // Verifica se já segue (retorna true/false)
    boolean existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(Long seguidorId, Long seguidoId);
    
    // 🔴 ESTE É O MÉTODO QUE FALTAVA (Busca o objeto para deletar)
    Optional<Seguindo> findBySeguidor_IdUsuarioAndSeguido_IdUsuario(Long seguidorId, Long seguidoId);
    
    // Contagens para o perfil
    long countBySeguido_IdUsuario(Long seguidoId); 
    long countBySeguidor_IdUsuario(Long seguidorId); 

    List<Seguindo> findAllBySeguidor_IdUsuario(Long seguidorId);
    void deleteBySeguidor_IdUsuario(Long idUsuario);
    void deleteBySeguido_IdUsuario(Long idUsuario);
}
