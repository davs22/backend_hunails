package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.LoginRequest;
import com.example.manicure_backend.DTO.UsuarioRequestDTO;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.security.JwtUtil;
import com.example.manicure_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRequestDTO dto) {
        try {
            Usuario novoUsuario = usuarioService.registrarUsuario(dto);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", novoUsuario.getIdUsuario());
            response.put("nome", novoUsuario.getNome());
            response.put("email", novoUsuario.getEmail());
            response.put("telefone", novoUsuario.getTelefone());
            response.put("whatsapp", novoUsuario.getWhatsapp());
            response.put("cep", novoUsuario.getCep());
            response.put("bairro", novoUsuario.getBairro());
            response.put("cidade", novoUsuario.getCidade());
            response.put("estado", novoUsuario.getEstado());
            response.put("biografia", novoUsuario.getBiografia());
            response.put("latitude", novoUsuario.getLatitude());
            response.put("longitude", novoUsuario.getLongitude());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Dados invalidos"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return usuarioService.login(request.getEmail(), request.getSenha())
                .map(usuario -> {
                    String token = jwtUtil.generateToken(usuario.getEmail());
                    boolean isManicure = usuario.getComplemento() != null;

                    Map<String, Object> usuarioResponse = new LinkedHashMap<>();
                    usuarioResponse.put("id", usuario.getIdUsuario());
                    usuarioResponse.put("nome", usuario.getNome());
                    usuarioResponse.put("email", usuario.getEmail());
                    usuarioResponse.put("telefone", usuario.getTelefone());
                    usuarioResponse.put("whatsapp", usuario.getWhatsapp());
                    usuarioResponse.put("cep", usuario.getCep());
                    usuarioResponse.put("bairro", usuario.getBairro());
                    usuarioResponse.put("cidade", usuario.getCidade());
                    usuarioResponse.put("estado", usuario.getEstado());
                    usuarioResponse.put("biografia", usuario.getBiografia());
                    usuarioResponse.put("latitude", usuario.getLatitude());
                    usuarioResponse.put("longitude", usuario.getLongitude());
                    usuarioResponse.put("isManicure", isManicure);

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("token", token);
                    response.put("isManicure", isManicure);
                    response.put("usuario", usuarioResponse);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Email ou senha inválidos")));
    }
}
