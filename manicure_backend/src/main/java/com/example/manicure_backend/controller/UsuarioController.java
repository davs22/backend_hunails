package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.UsuarioDTO;
import com.example.manicure_backend.DTO.UsuarioRequestDTO;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.security.AuthenticatedUser;
import com.example.manicure_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorIdDTO(id, authenticatedUser.optionalId()));
    }

    @GetMapping("/nome")
    public ResponseEntity<List<UsuarioDTO>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(usuarioService.buscarPorNome(nome, authenticatedUser.optionalId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        Usuario atualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(usuarioService.toDTO(atualizado, atualizado.getIdUsuario()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> excluirMinhaConta() {
        usuarioService.excluirConta(authenticatedUser.require().getIdUsuario());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/manicures")
    public ResponseEntity<List<UsuarioDTO>> listarManicures(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "bairro", required = false) String bairro,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado) {
        return ResponseEntity.ok(usuarioService.listarApenasManicures(q, bairro, cidade, estado,
                authenticatedUser.optionalId()));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.buscarPorNome("", authenticatedUser.optionalId()));
    }
}
