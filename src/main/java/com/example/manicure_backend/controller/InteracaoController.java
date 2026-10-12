package com.example.manicure_backend.controller;

import com.example.manicure_backend.model.Comentario;
import com.example.manicure_backend.model.Curtida;
import com.example.manicure_backend.model.Post;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.ComentarioRepository;
import com.example.manicure_backend.repository.CurtidaRepository;
import com.example.manicure_backend.repository.PostRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import com.example.manicure_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class InteracaoController {

    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurtidaRepository curtidaRepository;
    private final ComentarioRepository comentarioRepository;
    private final JwtUtil jwtUtil;

    private Usuario getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = getUserFromToken(authHeader);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        var curtidaExistente = curtidaRepository.findByUsuarioAndPost(usuario, post);

        if (curtidaExistente.isPresent()) {
            curtidaRepository.delete(curtidaExistente.get());
            return ResponseEntity.ok(Map.of("liked", false));
        }

        Curtida nova = Curtida.builder().usuario(usuario).post(post).build();
        curtidaRepository.save(nova);
        return ResponseEntity.ok(Map.of("liked", true));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        Usuario usuario = getUserFromToken(authHeader);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        String texto = body.get("texto");
        if (texto == null || texto.isBlank()) {
            return ResponseEntity.badRequest().body("Texto obrigatório");
        }

        Comentario comentario = Comentario.builder()
                .usuario(usuario)
                .post(post)
                .texto(texto)
                .data(java.time.LocalDateTime.now())
                .build();

        comentarioRepository.save(comentario);

        Map<String, Object> response = new HashMap<>();
        response.put("id", comentario.getId());
        response.put("texto", comentario.getTexto());
        response.put("autor", usuario.getNome());
        response.put("autorId", usuario.getIdUsuario());
        response.put("autorFoto", usuario.getUrlFotoPerfil());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        List<Comentario> comentarios = comentarioRepository.findByPostOrderByDataDesc(post);

        var response = comentarios.stream().map(c -> {
            Map<String, Object> comment = new HashMap<>();
            comment.put("id", c.getId());
            comment.put("texto", c.getTexto());
            comment.put("autor", c.getUsuario().getNome());
            comment.put("autorId", c.getUsuario().getIdUsuario());
            comment.put("autorFoto", c.getUsuario().getUrlFotoPerfil());
            return comment;
        }).toList();

        return ResponseEntity.ok(response);
    }
}
