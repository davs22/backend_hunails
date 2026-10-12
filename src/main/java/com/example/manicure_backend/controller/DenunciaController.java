package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.DenunciaRequestDTO;
import com.example.manicure_backend.service.DenunciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaService denunciaService;

    @PostMapping
    public ResponseEntity<Void> denunciar(
            @RequestBody DenunciaRequestDTO dto,
            @RequestHeader("Authorization") String authHeader
    ) {
        denunciaService.denunciar(authHeader, dto);
        return ResponseEntity.ok().build();
    }
}
