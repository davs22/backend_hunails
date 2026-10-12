package com.example.manicure_backend.service;

import com.example.manicure_backend.DTO.AgendamentoDTO;
import com.example.manicure_backend.DTO.AgendamentoRequestDTO;
import com.example.manicure_backend.model.Agendamento;
import com.example.manicure_backend.model.StatusAgendamento;
import com.example.manicure_backend.model.Usuario;
import com.example.manicure_backend.repository.AgendamentoRepository;
import com.example.manicure_backend.repository.ComplementosRepository;
import com.example.manicure_backend.repository.UsuarioRepository;
import com.example.manicure_backend.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    private final AgendamentoRepository repository;
    private final UsuarioRepository usuarios;
    private final ComplementosRepository complementos;
    private final AuthenticatedUser authenticatedUser;
    private final UsuarioService usuarioService;

    private Agendamento requireParticipant(Long id, Long requesterId) {
        Agendamento agendamento = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!agendamento.getUsuario().getIdUsuario().equals(requesterId)
                && !agendamento.getManicure().getIdUsuario().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return agendamento;
    }

    private AgendamentoDTO toDTO(Agendamento entity, Long requesterId) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setIdAgendamento(entity.getIdAgendamento());
        dto.setDescricao(entity.getDescricao());
        dto.setData(entity.getData());
        dto.setHora(entity.getHora());
        dto.setValor(entity.getValor());
        dto.setStatus(entity.getStatus());
        dto.setUsuario(usuarioService.toDTO(entity.getUsuario(), requesterId));
        dto.setManicure(usuarioService.toDTO(entity.getManicure(), requesterId));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarMeusAgendamentos() {
        Long id = authenticatedUser.require().getIdUsuario();
        return repository.findByUsuario_IdUsuario(id).stream().map(a -> toDTO(a, id)).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarAgendaManicurePeloLogin() {
        Long id = authenticatedUser.require().getIdUsuario();
        return repository.findByManicure_IdUsuario(id).stream().map(a -> toDTO(a, id)).toList();
    }

    @Transactional
    public AgendamentoDTO criar(AgendamentoRequestDTO dto) {
        Usuario cliente = authenticatedUser.require();
        if (dto == null || dto.getManicureId() == null || dto.getData() == null || dto.getHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Usuario manicure = usuarios.findById(dto.getManicureId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (complementos.findByUsuario_IdUsuario(manicure.getIdUsuario()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario selecionado nao e manicure");
        }
        if (repository.existsByManicure_IdUsuarioAndDataAndHora(manicure.getIdUsuario(), dto.getData(), dto.getHora())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horario ocupado");
        }
        Agendamento agendamento = new Agendamento();
        agendamento.setUsuario(cliente);
        agendamento.setManicure(manicure);
        agendamento.setDescricao(dto.getDescricao());
        agendamento.setData(dto.getData());
        agendamento.setHora(dto.getHora());
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setValor(0.0);
        return toDTO(repository.save(agendamento), cliente.getIdUsuario());
    }

    @Transactional
    public void deletar(Long id) {
        Long requesterId = authenticatedUser.require().getIdUsuario();
        repository.delete(requireParticipant(id, requesterId));
    }

    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Long id) {
        Long requesterId = authenticatedUser.require().getIdUsuario();
        return toDTO(requireParticipant(id, requesterId), requesterId);
    }
}
