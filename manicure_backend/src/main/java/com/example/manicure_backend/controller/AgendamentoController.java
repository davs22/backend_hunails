package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.AgendamentoDTO;
import com.example.manicure_backend.DTO.AgendamentoRequestDTO;
import com.example.manicure_backend.service.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;

    @GetMapping("/meus")
    public ResponseEntity<List<AgendamentoDTO>> listarMeusAgendamentos() {
        return ResponseEntity.ok(service.listarMeusAgendamentos());
    }

    @GetMapping("/minha-agenda")
    public ResponseEntity<List<AgendamentoDTO>> listarAgendaManicure() {
        return ResponseEntity.ok(service.listarAgendaManicurePeloLogin());
    }

    @PostMapping
    public ResponseEntity<AgendamentoDTO> criar(@RequestBody AgendamentoRequestDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}