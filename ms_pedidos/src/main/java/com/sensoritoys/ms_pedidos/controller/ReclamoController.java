package com.sensoritoys.ms_pedidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sensoritoys.ms_pedidos.dto.ReclamoEstadoRequestDTO;
import com.sensoritoys.ms_pedidos.dto.ReclamoRequestDTO;
import com.sensoritoys.ms_pedidos.model.Reclamo;
import com.sensoritoys.ms_pedidos.service.ReclamoService;

@RestController
@RequestMapping("/api/v1/reclamos")
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping
    public ResponseEntity<Reclamo> crear(@RequestBody ReclamoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reclamoService.crearReclamo(request));
    }

    @GetMapping
    public ResponseEntity<List<Reclamo>> listar() {
        return ResponseEntity.ok(reclamoService.listarReclamos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Reclamo>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(reclamoService.listarPorUsuario(usuarioId));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Reclamo> actualizarEstado(@PathVariable Long id, @RequestBody ReclamoEstadoRequestDTO request) {
        return ResponseEntity.ok(reclamoService.actualizarEstado(id, request.getEstado()));
    }
}
