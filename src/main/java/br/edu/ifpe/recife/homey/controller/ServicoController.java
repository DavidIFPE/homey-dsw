package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.AtualizarServicoDTO;
import br.edu.ifpe.recife.homey.dto.CriarServicoDTO;
import br.edu.ifpe.recife.homey.dto.ServicoResponseDTO;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.service.ServicoService;
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
@RequestMapping("/servicos")
@Tag(name = "Serviços", description = "Gerenciamento de serviços oferecidos por prestadores")
public class ServicoController {
    
    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    @Operation(summary = "Listar serviços", description = "Endpoint público - lista todos os serviços ou filtra por prestador")
    public ResponseEntity<List<ServicoResponseDTO>> listarTodos(
            @Parameter(description = "ID do prestador para filtrar serviços")
            @RequestParam(required = false) Long prestadorId) {
        List<ServicoResponseDTO> servicos;
        
        if (prestadorId != null) {
            servicos = servicoService.listarPorPrestador(prestadorId).stream()
                    .map(ServicoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            servicos = servicoService.listarTodos().stream()
                    .map(ServicoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        try {
            ServicoResponseDTO servico = ServicoResponseDTO.fromEntity(
                    servicoService.buscarPorId(id)
            );
            return ResponseEntity.ok(servico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar serviço", description = "Cria um novo serviço - apenas PRESTADOR autenticado")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> criar(
            @Valid @RequestBody CriarServicoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            ServicoResponseDTO servico = ServicoResponseDTO.fromEntity(
                    servicoService.criar(dto, usuarioLogado)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(servico);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarServicoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            ServicoResponseDTO servico = ServicoResponseDTO.fromEntity(
                    servicoService.atualizar(id, dto, usuarioLogado)
            );
            return ResponseEntity.ok(servico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            servicoService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<?> alterarDisponibilidade(
            @PathVariable Long id,
            @RequestParam Boolean disponivel,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            ServicoResponseDTO servico = ServicoResponseDTO.fromEntity(
                    servicoService.alterarDisponibilidade(id, disponivel, usuarioLogado)
            );
            return ResponseEntity.ok(servico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
