package br.edu.ifpe.recife.homey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/prestador")
    public ResponseEntity<?> criarPrestador(@Valid @RequestBody CriarPrestadorDTO dto) throws Exception {
        return ResponseEntity.ok(usuarioService.criaPrestador(dto));
    }

    @PostMapping("/cliente")
    public ResponseEntity<?> criarCliente(@Valid @RequestBody CriarClienteDTO dto) throws Exception {
        return ResponseEntity.ok(usuarioService.criaCliente(dto));
    }
}
