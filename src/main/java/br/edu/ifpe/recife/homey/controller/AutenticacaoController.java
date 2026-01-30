package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.AutenticacaoDTO;
import br.edu.ifpe.recife.homey.dto.LoginResponseDTO;
import br.edu.ifpe.recife.homey.dto.RegistroDTO;
import br.edu.ifpe.recife.homey.dto.UsuarioResponseDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.security.TokenService;
import br.edu.ifpe.recife.homey.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/autenticacao")
@Tag(name = "Autenticação", description = "Endpoints para autenticação, registro e informações do usuário logado")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica um usuário (Cliente ou Prestador) e retorna um token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Email ou senha inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> login(@RequestBody @Valid AutenticacaoDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            var auth = authenticationManager.authenticate(usernamePassword);

            String token = tokenService.generateToken((Usuario) auth.getPrincipal());

            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou senha inválidos");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar login");
        }
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar novo usuário", description = "Registra um novo Cliente ou Prestador no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado"),
        @ApiResponse(responseCode = "500", description = "Erro ao registrar usuário")
    })
    public ResponseEntity<?> registro(@RequestBody @Valid RegistroDTO data) {
        try {
            Usuario usuario = autenticacaoService.registrarUsuario(data);
            String tipoUsuario = usuario instanceof Cliente ? "Cliente" : "Prestador";
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(tipoUsuario + " registrado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao registrar usuário: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário logado", description = "Retorna os dados completos do usuário autenticado")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso",
                content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<UsuarioResponseDTO> usuarioLogado(@AuthenticationPrincipal Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuarioLogado));
    }
}
