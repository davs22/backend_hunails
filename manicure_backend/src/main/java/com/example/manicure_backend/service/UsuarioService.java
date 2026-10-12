package com.example.manicure_backend.service;

import com.example.manicure_backend.DTO.UsuarioDTO;
import com.example.manicure_backend.DTO.UsuarioRequestDTO;
import com.example.manicure_backend.security.AuthenticatedUser;
import com.example.manicure_backend.model.Complementos;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.model.Sexo;
import com.example.manicure_backend.repository.ComplementosRepository;
import com.example.manicure_backend.repository.AnalyticsEventRepository;
import com.example.manicure_backend.repository.AgendamentoRepository;
import com.example.manicure_backend.repository.ComentarioRepository;
import com.example.manicure_backend.repository.CurtidaRepository;
import com.example.manicure_backend.repository.DenunciaRepository;
import com.example.manicure_backend.repository.PostRepository;
import com.example.manicure_backend.repository.SeguindoRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ComplementosRepository complementosRepository;
    private final SeguindoRepository seguindoRepository;
    private final PostRepository postRepository;
    private final ComentarioRepository comentarioRepository;
    private final CurtidaRepository curtidaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final DenunciaRepository denunciaRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUser authenticatedUser;

    private static final Set<String> OFFENSIVE_TERMS = Set.of(
            "puta", "porra", "caralho", "buceta", "piranha", "vagabunda", "merda"
    );

    private String resolveRegiao(Usuario user) {
        String bairro = user.getBairro();
        String cidade = user.getCidade();
        String estado = user.getEstado();

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
        if (user.getComplemento() != null && user.getComplemento().getRegiao() != null && !user.getComplemento().getRegiao().isBlank()) {
            return user.getComplemento().getRegiao();
        }
        return null;
    }

    private boolean containsOffensiveTerm(String value) {
        String normalized = value == null ? "" : value.toLowerCase();
        return OFFENSIVE_TERMS.stream().anyMatch(normalized::contains);
    }

    private void validateUsuarioDTO(UsuarioRequestDTO dto, boolean isUpdate) {
        if (!isUpdate || dto.getNome() != null) {
            String nome = dto.getNome() == null ? "" : dto.getNome().trim();
            if (nome.length() < 2 || nome.length() > 80) {
                throw new IllegalArgumentException("Nome invalido.");
            }
            if (containsOffensiveTerm(nome)) {
                throw new IllegalArgumentException("O nome informado viola nossas regras.");
            }
        }

        if (dto.getIdade() != null && (dto.getIdade() < 13 || dto.getIdade() > 120)) {
            throw new IllegalArgumentException("Idade invalida.");
        }

        if (dto.getBiografia() != null && dto.getBiografia().length() > 500) {
            throw new IllegalArgumentException("Biografia muito longa.");
        }

        if (dto.getUrlFotoPerfil() != null && dto.getUrlFotoPerfil().length() > 8_000_000) {
            throw new IllegalArgumentException("Imagem muito grande.");
        }
    }

    private int recommendationScore(Usuario target, Usuario currentUser) {
        if (currentUser == null) {
            return 0;
        }

        int score = 0;
        Long currentId = currentUser.getIdUsuario();
        Long targetId = target.getIdUsuario();

        if (seguindoRepository.existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(currentId, targetId)) {
            score += 100;
        }
        if (currentUser.getCidade() != null && target.getCidade() != null
                && currentUser.getCidade().equalsIgnoreCase(target.getCidade())) {
            score += 30;
        }
        if (currentUser.getEstado() != null && target.getEstado() != null
                && currentUser.getEstado().equalsIgnoreCase(target.getEstado())) {
            score += 15;
        }
        if (currentUser.getComplemento() != null
                && target.getComplemento() != null
                && currentUser.getComplemento().getEspecialidade() != null
                && target.getComplemento().getEspecialidade() != null
                && currentUser.getComplemento().getEspecialidade().equalsIgnoreCase(target.getComplemento().getEspecialidade())) {
            score += 10;
        }
        return score;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(List.of())
                .build();
    }

    public Optional<Usuario> login(String email, String senha) {
        return usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(senha, u.getSenha()));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO toDTO(Usuario user, Long meuId) {
        Complementos comp = user.getComplemento();
        boolean isManicure = comp != null;

        long seguidores = seguindoRepository.countBySeguido_IdUsuario(user.getIdUsuario());
        long seguindo = seguindoRepository.countBySeguidor_IdUsuario(user.getIdUsuario());
        boolean sigoEle = meuId != null
                && seguindoRepository.existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(meuId, user.getIdUsuario());

        return UsuarioDTO.builder()
                .idUsuario(user.getIdUsuario())
                .nome(user.getNome())
                .email(user.getIdUsuario().equals(meuId) ? user.getEmail() : null)
                .idade(user.getIdUsuario().equals(meuId) ? user.getIdade() : null)
                .sexo(user.getIdUsuario().equals(meuId) ? user.getSexo() : null)
                .urlFotoPerfil(user.getUrlFotoPerfil())
                .telefone(isManicure || user.getIdUsuario().equals(meuId) ? user.getTelefone() : null)
                .whatsapp(isManicure || user.getIdUsuario().equals(meuId) ? user.getWhatsapp() : null)
                .cep(user.getIdUsuario().equals(meuId) ? user.getCep() : null)
                .bairro(isManicure || user.getIdUsuario().equals(meuId) ? user.getBairro() : null)
                .cidade(isManicure || user.getIdUsuario().equals(meuId) ? user.getCidade() : null)
                .estado(isManicure || user.getIdUsuario().equals(meuId) ? user.getEstado() : null)
                .biografia(user.getBiografia())
                .latitude(isManicure || user.getIdUsuario().equals(meuId) ? user.getLatitude() : null)
                .longitude(isManicure || user.getIdUsuario().equals(meuId) ? user.getLongitude() : null)
                .especialidade(isManicure ? comp.getEspecialidade() : null)
                .regiao(isManicure || user.getIdUsuario().equals(meuId) ? resolveRegiao(user) : null)
                .isManicure(isManicure)
                .seguidores(isManicure ? seguidores : 0)
                .seguindo(isManicure ? seguindo : 0)
                .seguidoPorMim(isManicure && sigoEle)
                .build();
    }

    public List<UsuarioDTO> listarApenasManicures(String q, String bairro, String cidade, String estado, Long meuId) {
        Usuario currentUser = meuId == null ? null : usuarioRepository.findById(meuId).orElse(null);

        return usuarioRepository.findFilteredManicures(q, bairro, cidade, estado).stream()
                .sorted((a, b) -> Integer.compare(recommendationScore(b, currentUser), recommendationScore(a, currentUser)))
                .map(u -> toDTO(u, meuId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Usuario registrarUsuario(UsuarioRequestDTO dto) {
        validateUsuarioDTO(dto, false);
        if (dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new IllegalArgumentException("Email e senha obrigatorios.");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .idade(dto.getIdade())
                .sexo(dto.getSexo() == null ? Sexo.F : dto.getSexo())
                .urlFotoPerfil(dto.getUrlFotoPerfil())
                .telefone(dto.getTelefone())
                .whatsapp(dto.getWhatsapp())
                .cep(dto.getCep())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .biografia(dto.getBiografia())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .build();

        usuario = usuarioRepository.save(usuario);

        if (dto.getEspecialidade() != null && !dto.getEspecialidade().trim().isEmpty()) {
            Complementos c = new Complementos();
            c.setUsuario(usuario);
            c.setEspecialidade(dto.getEspecialidade());
            c.setRegiao(dto.getRegiao());
            complementosRepository.save(c);
            usuario.setComplemento(c);
        }
        return usuario;
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequestDTO dto) {
        authenticatedUser.requireOwner(id);
        validateUsuarioDTO(dto, true);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getIdade() != null) {
            usuario.setIdade(dto.getIdade());
        }
        if (dto.getSexo() != null) {
            usuario.setSexo(dto.getSexo());
        }
        if (dto.getUrlFotoPerfil() != null) {
            usuario.setUrlFotoPerfil(dto.getUrlFotoPerfil());
        }
        if (dto.getTelefone() != null) {
            usuario.setTelefone(dto.getTelefone());
        }
        if (dto.getWhatsapp() != null) {
            usuario.setWhatsapp(dto.getWhatsapp());
        }
        if (dto.getCep() != null) {
            usuario.setCep(dto.getCep());
        }
        if (dto.getBairro() != null) {
            usuario.setBairro(dto.getBairro());
        }
        if (dto.getCidade() != null) {
            usuario.setCidade(dto.getCidade());
        }
        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }
        if (dto.getBiografia() != null) {
            usuario.setBiografia(dto.getBiografia());
        }
        if (dto.getLatitude() != null) {
            usuario.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            usuario.setLongitude(dto.getLongitude());
        }
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        if (dto.getEspecialidade() != null && !dto.getEspecialidade().isBlank()) {
            Complementos comp = usuario.getComplemento();
            if (comp == null) {
                comp = new Complementos();
                comp.setUsuario(usuario);
            }
            comp.setEspecialidade(dto.getEspecialidade());
            comp.setRegiao(dto.getRegiao());
            complementosRepository.save(comp);
            usuario.setComplemento(comp);
        } else if (dto.getEspecialidade() != null && dto.getEspecialidade().isEmpty()) {
            if (usuario.getComplemento() != null) {
                complementosRepository.delete(usuario.getComplemento());
                usuario.setComplemento(null);
            }
        }

        return usuarioRepository.save(usuario);
    }

    public UsuarioDTO buscarPorIdDTO(Long id, Long meuId) {
        return toDTO(usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)), meuId);
    }

    public List<UsuarioDTO> buscarPorNome(String nome, Long meuId) {
        String query = nome == null ? "" : nome;
        return usuarioRepository.searchUsers(query).stream()
                .map(u -> toDTO(u, meuId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void excluirConta(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        List<Long> postIds = postRepository.findAllByAuthor_IdUsuario(id).stream()
                .map(UsuarioService::extractPostId)
                .collect(Collectors.toList());

        analyticsEventRepository.deleteByUsuario_IdUsuario(id);
        denunciaRepository.deleteByDenunciante_IdUsuario(id);
        denunciaRepository.deleteByUsuarioAlvo_IdUsuario(id);

        if (!postIds.isEmpty()) {
            denunciaRepository.deleteByPostAlvo_IdPostIn(postIds);
            comentarioRepository.deleteByPost_IdPostIn(postIds);
            curtidaRepository.deleteByPost_IdPostIn(postIds);
            postRepository.deleteAllById(postIds);
        }

        comentarioRepository.deleteByUsuario_IdUsuario(id);
        curtidaRepository.deleteByUsuario_IdUsuario(id);
        seguindoRepository.deleteBySeguidor_IdUsuario(id);
        seguindoRepository.deleteBySeguido_IdUsuario(id);
        agendamentoRepository.deleteByUsuario_IdUsuario(id);
        agendamentoRepository.deleteByManicure_IdUsuario(id);

        if (usuario.getComplemento() != null) {
            complementosRepository.delete(usuario.getComplemento());
        }

        usuarioRepository.delete(usuario);
    }

    private static Long extractPostId(com.example.manicure_backend.model.Post post) {
        return post.getIdPost();
    }
}
