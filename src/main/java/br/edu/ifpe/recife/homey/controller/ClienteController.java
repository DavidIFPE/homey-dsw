package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cliente")
public class ClienteController {
    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);
    private final UsuarioService usuarioService;

    public ClienteController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> criarCliente(@Valid @RequestBody CriarClienteDTO dto) throws Exception {
        return ResponseEntity.ok(usuarioService.criaCliente(dto));
    }
}
