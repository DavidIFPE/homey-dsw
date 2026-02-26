package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.AvaliacaoResponseDTO;
import br.edu.ifpe.recife.homey.dto.CriarAvaliacaoDTO;
import br.edu.ifpe.recife.homey.entity.Avaliacao;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/avaliacoes")
@Tag(name = "Avaliações", description = "Gerenciamento de avaliações de contratos")
@SecurityRequirement(name = "Bearer Authentication")
public class AvaliacaoController {
    
    private final AvaliacaoService avaliacaoService;
    
    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Criar avaliação", description = "Cliente cria avaliação de um contrato concluído com foto opcional")
    public ResponseEntity<?> criar(
            @RequestParam Long contratoId,
            @RequestParam Integer nota,
            @RequestParam(required = false) String comentario,
            @RequestParam(value = "foto", required = false)
            @Schema(type = "string", format = "binary", description = "Arquivo de imagem do serviço")
            MultipartFile foto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            CriarAvaliacaoDTO dto = new CriarAvaliacaoDTO(contratoId, nota, comentario);
            Avaliacao avaliacaoCriada = avaliacaoService.criar(dto, foto, usuarioLogado);
            AvaliacaoResponseDTO avaliacao = AvaliacaoResponseDTO.fromEntity(avaliacaoCriada);
            return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    @GetMapping("/contrato/{contratoId}")
    @Operation(summary = "Buscar avaliação de um contrato", description = "Obtém a avaliação de um contrato específico")
    public ResponseEntity<?> buscarPorContrato(
            @PathVariable Long contratoId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            AvaliacaoResponseDTO avaliacao = AvaliacaoResponseDTO.fromEntity(
                    avaliacaoService.buscarPorContrato(contratoId, usuarioLogado)
            );
            return ResponseEntity.ok(avaliacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar avaliações de um cliente", description = "Lista todas as avaliações criadas por um cliente")
    public ResponseEntity<?> listarPorCliente(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            List<AvaliacaoResponseDTO> avaliacoes = avaliacaoService.listarPorCliente(clienteId, usuarioLogado)
                    .stream()
                    .map(AvaliacaoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(avaliacoes);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    
    @GetMapping("/prestador/{prestadorId}")
    @Operation(summary = "Listar avaliações de um prestador", description = "Lista todas as avaliações recebidas por um prestador")
    public ResponseEntity<?> listarPorPrestador(
            @PathVariable Long prestadorId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            List<AvaliacaoResponseDTO> avaliacoes = avaliacaoService.listarPorPrestador(prestadorId, usuarioLogado)
                    .stream()
                    .map(AvaliacaoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(avaliacoes);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar avaliação", description = "Apenas o cliente que fez a avaliação pode deletar")
    public ResponseEntity<?> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        try {
            avaliacaoService.deletar(id, usuarioLogado);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
