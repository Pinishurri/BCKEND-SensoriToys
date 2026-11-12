package com.sensoritoys.ms_usuarios.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sensoritoys.ms_usuarios.dto.LoginRequestDTO;
import com.sensoritoys.ms_usuarios.dto.LoginResponseDTO;
import com.sensoritoys.ms_usuarios.dto.UsuarioRequestDTO;
import com.sensoritoys.ms_usuarios.dto.UsuarioResponseDTO;
import com.sensoritoys.ms_usuarios.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO nuevoUsuario = UsuarioResponseDTO.desde(usuarioService.registrarUsuario(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.listarUsuarios().stream()
                .map(UsuarioResponseDTO::desde)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(UsuarioResponseDTO::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(UsuarioResponseDTO.desde(usuarioService.actualizarUsuario(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLogicamente(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/rol/{idRol}")
    public ResponseEntity<UsuarioResponseDTO> asignarRol(@PathVariable Long id, @PathVariable Long idRol) {
        return ResponseEntity.ok(UsuarioResponseDTO.desde(usuarioService.asignarRol(id, idRol)));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(usuarioService.iniciarSesion(request.getEmail(), request.getContrasenia()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(@RequestBody String token) {
        usuarioService.cerrarSesion(token);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        usuarioService.activarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
