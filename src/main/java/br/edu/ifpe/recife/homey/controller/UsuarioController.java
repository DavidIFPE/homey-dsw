package br.edu.ifpe.recife.homey.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.dto.UsuarioResponseDTO;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;


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

    @GetMapping("/prestador/{id}")
    public ResponseEntity<?> pegarPrestador(@PathVariable("id") Long id) {
        Prestador prestador = usuarioService.pegarPrestador(id);
        UsuarioResponseDTO prestadorDTO = UsuarioResponseDTO.fromEntity(prestador);
        return ResponseEntity.ok(prestadorDTO);
    }
    

    @PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFoto(@AuthenticationPrincipal Usuario usuario,
                                        @RequestParam("foto") MultipartFile foto) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuário não autenticado"));
        }
        try {
            String fotoUrl = usuarioService.uploadFoto(usuario, foto);
            return ResponseEntity.ok(Map.of("fotoUrl", fotoUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao salvar foto"));
        }
    }
    
}
