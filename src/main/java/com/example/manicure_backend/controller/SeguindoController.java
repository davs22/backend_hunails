package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.SeguindoRequestDTO;
import com.example.manicure_backend.repository.UsuarioRepository;
import com.example.manicure_backend.service.SeguindoService;
import lombok.RequiredArgsConstructor; // <--- IMPORTANTE
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor // <--- ISSO CORRIGE O ERRO 500 (Injeta os repositórios automaticamente)
public class SeguindoController {

    private final SeguindoService seguindoService;
    private final UsuarioRepository usuarioRepository; // <--- Agora não será mais nulo!

    private Long getRequesterId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        
        // O JWT Filter define o principal como String (email)
        String email = authentication.getPrincipal().toString();
        
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado no banco"))
                .getIdUsuario();
    }

    // 1. SEGUIR
    @PostMapping
    public ResponseEntity<Void> follow(@RequestBody SeguindoRequestDTO request) {
        try {
            Long seguidorId = getRequesterId();
            // System.out.println("Seguindo ID: " + request.getSeguidoId()); // Debug se precisar
            seguindoService.follow(seguidorId, request.getSeguidoId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build(); // Erro de regra (já segue, etc)
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build(); // Usuário alvo não existe
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. DEIXAR DE SEGUIR
    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestBody SeguindoRequestDTO request) {
        try {
            Long seguidorId = getRequesterId();
            seguindoService.unfollow(seguidorId, request.getSeguidoId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. STATUS
    @GetMapping("/status/{id}")
    public ResponseEntity<Boolean> getFollowingStatus(@PathVariable Long id) {
        try {
            Long seguidorId = getRequesterId();
            if (seguidorId.equals(id)) return ResponseEntity.ok(true);
            
            boolean isFollowing = seguindoService.isFollowing(seguidorId, id);
            return ResponseEntity.ok(isFollowing);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}
