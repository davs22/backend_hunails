package com.example.manicure_backend.service;

import com.example.manicure_backend.DTO.PostDTO;
import com.example.manicure_backend.model.Curtida;
import com.example.manicure_backend.model.Post;
import com.example.manicure_backend.model.Seguindo;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.CurtidaRepository;
import com.example.manicure_backend.repository.PostRepository;
import com.example.manicure_backend.repository.SeguindoRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import com.example.manicure_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurtidaRepository curtidaRepository;
    private final SeguindoRepository seguindoRepository;
    private final JwtUtil jwtUtil;

    private String resolveRegiao(Usuario autor) {
        String bairro = autor.getBairro();
        String cidade = autor.getCidade();
        String estado = autor.getEstado();

        if (bairro != null && !bairro.isBlank() && cidade != null && !cidade.isBlank()) {
            return estado != null && !estado.isBlank()
                    ? bairro + " - " + cidade + "/" + estado
                    : bairro + " - " + cidade;
        }
        if (cidade != null && !cidade.isBlank()) {
            return estado != null && !estado.isBlank()
                    ? cidade + "/" + estado
                    : cidade;
        }
        if (autor.getComplemento() != null) {
            return autor.getComplemento().getRegiao();
        }
        return null;
    }

    private PostDTO toDTO(Post post, Usuario currentUser) {
        long likes = curtidaRepository.countByPost(post);
        boolean liked = currentUser != null && curtidaRepository.existsByUsuarioAndPost(currentUser, post);
        Usuario autor = post.getAuthor();

        return new PostDTO(
                post.getIdPost(),
                post.getTitulo(),
                post.getDescricao(),
                post.getUrlImagem(),
                post.getData(),
                autor.getIdUsuario(),
                autor.getNome(),
                autor.getUrlFotoPerfil(),
                resolveRegiao(autor),
                likes,
                liked
        );
    }

    private Usuario resolveCurrentUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String email = jwtUtil.extractEmail(token.substring(7));
                return usuarioRepository.findByEmail(email).orElse(null);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private int recommendationScore(Post post, Usuario currentUser, Set<Long> followedIds, Set<Long> likedAuthorIds) {
        if (currentUser == null) {
            return 0;
        }

        int score = 0;
        Long authorId = post.getAuthor().getIdUsuario();

        if (followedIds.contains(authorId)) {
            score += 100;
        }
        if (likedAuthorIds.contains(authorId)) {
            score += 45;
        }
        if (currentUser.getCidade() != null && post.getAuthor().getCidade() != null
                && currentUser.getCidade().equalsIgnoreCase(post.getAuthor().getCidade())) {
            score += 25;
        }
        if (currentUser.getEstado() != null && post.getAuthor().getEstado() != null
                && currentUser.getEstado().equalsIgnoreCase(post.getAuthor().getEstado())) {
            score += 10;
        }
        if (post.getAuthor().getComplemento() != null && post.getAuthor().getComplemento().getEspecialidade() != null) {
            String authorEspecialidade = post.getAuthor().getComplemento().getEspecialidade().toLowerCase();
            if ((post.getTitulo() != null && post.getTitulo().toLowerCase().contains(authorEspecialidade))
                    || (post.getDescricao() != null && post.getDescricao().toLowerCase().contains(authorEspecialidade))) {
                score += 5;
            }
        }
        return score;
    }

    @Transactional(readOnly = true)
    public List<PostDTO> listarTodosDTO(String token, String q, String bairro, String cidade, String estado) {
        Usuario currentUser = resolveCurrentUser(token);
        Set<Long> followedIds = currentUser == null
                ? Set.of()
                : seguindoRepository.findAllBySeguidor_IdUsuario(currentUser.getIdUsuario()).stream()
                .map(Seguindo::getSeguido)
                .map(Usuario::getIdUsuario)
                .collect(Collectors.toSet());
        Set<Long> likedAuthorIds = currentUser == null
                ? Set.of()
                : curtidaRepository.findAllByUsuario(currentUser).stream()
                .map(Curtida::getPost)
                .map(Post::getAuthor)
                .map(Usuario::getIdUsuario)
                .collect(Collectors.toSet());

        Usuario finalUser = currentUser;
        return postRepository.findFilteredPosts(q, bairro, cidade, estado).stream()
                .sorted((a, b) -> Integer.compare(
                        recommendationScore(b, finalUser, followedIds, likedAuthorIds),
                        recommendationScore(a, finalUser, followedIds, likedAuthorIds)
                ))
                .map(p -> toDTO(p, finalUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarTodosPaginadoDTO(
            String token,
            String q,
            String bairro,
            String cidade,
            String estado,
            int page,
            int size
    ) {
        Usuario currentUser = resolveCurrentUser(token);
        Set<Long> followedIds = currentUser == null
                ? Set.of()
                : seguindoRepository.findAllBySeguidor_IdUsuario(currentUser.getIdUsuario()).stream()
                .map(Seguindo::getSeguido)
                .map(Usuario::getIdUsuario)
                .collect(Collectors.toSet());
        Set<Long> likedAuthorIds = currentUser == null
                ? Set.of()
                : curtidaRepository.findAllByUsuario(currentUser).stream()
                .map(Curtida::getPost)
                .map(Post::getAuthor)
                .map(Usuario::getIdUsuario)
                .collect(Collectors.toSet());

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 30));
        Page<Post> postPage = postRepository.findFilteredPostsPage(q, bairro, cidade, estado, pageable);
        Usuario finalUser = currentUser;

        List<PostDTO> items = postPage.getContent().stream()
                .sorted((a, b) -> Integer.compare(
                        recommendationScore(b, finalUser, followedIds, likedAuthorIds),
                        recommendationScore(a, finalUser, followedIds, likedAuthorIds)
                ))
                .map(p -> toDTO(p, finalUser))
                .collect(Collectors.toList());

        return Map.of(
                "items", items,
                "page", postPage.getNumber(),
                "size", postPage.getSize(),
                "hasNext", postPage.hasNext(),
                "totalElements", postPage.getTotalElements()
        );
    }

    public Post salvar(Post post, String token) {
        String email = jwtUtil.extractEmail(token);
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        if (usuario.getComplemento() == null) {
            throw new RuntimeException("Apenas manicures.");
        }
        post.setAuthor(usuario);
        return postRepository.save(post);
    }

    @Transactional
    public PostDTO salvarDTO(Post post, String token) {
        Post saved = salvar(post, token);
        return toDTO(saved, saved.getAuthor());
    }

    public List<PostDTO> listarPostsPorUsuarioLogado(String token) {
        String email = jwtUtil.extractEmail(token);
        Usuario user = usuarioRepository.findByEmail(email).orElseThrow();
        return postRepository.findAllByAuthorEmail(email).stream()
                .map(p -> toDTO(p, user))
                .collect(Collectors.toList());
    }

    public Optional<PostDTO> buscarPorIdDTO(Long id) {
        return postRepository.findById(id).map(p -> toDTO(p, null));
    }

    public void deletar(Long id, String token) {
        String email = jwtUtil.extractEmail(token);
        Post post = postRepository.findById(id).orElseThrow();
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("Nao autorizado");
        }
        postRepository.delete(post);
    }
}
