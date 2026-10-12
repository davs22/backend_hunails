package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.PostDTO;
import com.example.manicure_backend.model.Post;
import com.example.manicure_backend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> listarTodos(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "bairro", required = false) String bairro,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado
    ) {
        return ResponseEntity.ok(postService.listarTodosDTO(authHeader, q, bairro, cidade, estado));
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> listarTodosPaginado(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "bairro", required = false) String bairro,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.listarTodosPaginadoDTO(authHeader, q, bairro, cidade, estado, page, size));
    }

    @PostMapping
    public ResponseEntity<?> criarPost(@RequestBody Post post, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            PostDTO salvo = postService.salvarDTO(post, token);
            return ResponseEntity.ok(salvo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body("Operacao nao permitida");
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<PostDTO>> listarMeusPosts(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            return ResponseEntity.ok(postService.listarPostsPorUsuarioLogado(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
