package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.ContratoResponseDTO;
import br.edu.ifpe.recife.homey.dto.CriarContratoDTO;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.service.ContratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contratos")
@Tag(name = "Contratos", description = "Gerenciamento de contratos entre clientes e prestadores")
@SecurityRequirement(name = "Bearer Authentication")
public class ContratoController {
    
    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping
    @Operation(summary = "Listar contratos do usuário", description = "Cliente vê seus contratos, Prestador vê contratos de seus serviços")
    public ResponseEntity<List<ContratoResponseDTO>> listarTodos(
            @AuthenticationPrincipal Usuario usuarioLogado) {
        List<ContratoResponseDTO> contratos = contratoService.listarTodos(usuarioLogado).stream()
                .map(ContratoResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            ContratoResponseDTO contrato = ContratoResponseDTO.fromEntity(
                    contratoService.buscarPorId(id, usuarioLogado)
            );
            return ResponseEntity.ok(contrato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Criar contrato", description = "Cria um novo contrato - apenas CLIENTE pode criar")
    public ResponseEntity<?> criar(
            @Valid @RequestBody CriarContratoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            ContratoResponseDTO contrato = ContratoResponseDTO.fromEntity(
                    contratoService.criar(dto, usuarioLogado)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(contrato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do contrato", description = "Apenas PRESTADOR pode atualizar - status: PENDENTE, ATIVO, CONCLUIDO, CANCELADO")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @Parameter(description = "Novo status: PENDENTE, ATIVO, CONCLUIDO ou CANCELADO")
            @RequestParam String status,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            Contrato.StatusContrato novoStatus = Contrato.StatusContrato.valueOf(status.toUpperCase());
            ContratoResponseDTO contrato = ContratoResponseDTO.fromEntity(
                    contratoService.atualizarStatus(id, novoStatus, usuarioLogado)
            );
            return ResponseEntity.ok(contrato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Status inválido ou contrato não encontrado");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> listarPorCliente(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            List<ContratoResponseDTO> contratos = contratoService.listarPorCliente(clienteId, usuarioLogado)
                    .stream()
                    .map(ContratoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(contratos);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<?> listarPorPrestador(
            @PathVariable Long prestadorId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            List<ContratoResponseDTO> contratos = contratoService.listarPorPrestador(prestadorId, usuarioLogado)
                    .stream()
                    .map(ContratoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(contratos);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
