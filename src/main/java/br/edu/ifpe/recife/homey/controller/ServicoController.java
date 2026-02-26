package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.AtualizarServicoDTO;
import br.edu.ifpe.recife.homey.dto.CriarServicoDTO;
import br.edu.ifpe.recife.homey.dto.ServicoProximoDTO;
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
    @Operation(summary = "Listar serviços", description = "Endpoint público - lista todos os serviços ou filtra por prestador e/ou categoria")
    public ResponseEntity<List<ServicoResponseDTO>> listarTodos(
            @Parameter(description = "ID do prestador para filtrar serviços")
            @RequestParam(required = false) Long prestadorId,
            @Parameter(description = "Nome da categoria para filtrar serviços (opcional)")
            @RequestParam(required = false) String categoria) {
        List<ServicoResponseDTO> servicos;
        
        List<br.edu.ifpe.recife.homey.entity.Servico> base;

        if (prestadorId != null) {
            base = servicoService.listarPorPrestador(prestadorId);
        } else {
            base = servicoService.listarTodos();
        }

        if (categoria != null) {
            String nome = categoria.trim();
            if (!nome.isEmpty()) {
                String nomeFinal = nome;
                base = base.stream()
                        .filter(s -> s.getCategorias() != null &&
                                s.getCategorias().stream()
                                        .anyMatch(c -> c.getNome() != null && c.getNome().equalsIgnoreCase(nomeFinal)))
                        .toList();
            }
        }

        servicos = base.stream()
                .map(ServicoResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/por-categoria")
    @Operation(summary = "Listar serviços por categoria", description = "Endpoint público - lista serviços filtrando pelo nome da categoria")
    public ResponseEntity<List<ServicoResponseDTO>> listarPorCategoria(
            @Parameter(description = "Nome da categoria (case insensitive)")
            @RequestParam String categoria
    ) {
        List<ServicoResponseDTO> servicos = servicoService.listarPorCategoriaNome(categoria).stream()
                .map(ServicoResponseDTO::fromEntity)
                .collect(Collectors.toList());

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

    @GetMapping("/proximos")
    @Operation(summary = "Buscar serviços próximos", description = "Busca serviços próximos a um CEP fornecido, podendo filtrar por categoria")
    @SecurityRequirement(name = "Bearer Authentication")
    public List<ServicoProximoDTO> buscarServicosPorCep(
            @RequestParam String cep,
            @Parameter(description = "Nome da categoria para filtrar serviços (opcional)")
            @RequestParam(required = false) String categoria
    ) {
        return servicoService.buscarPorCep(cep, categoria);
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
