package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.AutenticacaoDTO;
import br.edu.ifpe.recife.homey.dto.LoginResponseDTO;
import br.edu.ifpe.recife.homey.dto.RegistroDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.security.TokenService;
import br.edu.ifpe.recife.homey.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
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

    @PostMapping("/registrp")
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
}