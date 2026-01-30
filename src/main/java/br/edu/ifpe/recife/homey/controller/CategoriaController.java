package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.CategoriaResponseDTO;
import br.edu.ifpe.recife.homey.dto.CriarCategoriaDTO;
import br.edu.ifpe.recife.homey.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias de serviços")
public class CategoriaController {
    
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias", description = "Endpoint público - retorna todas as categorias cadastradas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        List<CategoriaResponseDTO> categorias = categoriaService.listarTodas()
                .stream()
                .map(CategoriaResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        try {
            CategoriaResponseDTO categoria = CategoriaResponseDTO.fromEntity(
                    categoriaService.buscarPorId(id)
            );
            return ResponseEntity.ok(categoria);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria - endpoint público")
    public ResponseEntity<CategoriaResponseDTO> criar(@Valid @RequestBody CriarCategoriaDTO dto) {
        CategoriaResponseDTO categoria = CategoriaResponseDTO.fromEntity(
                categoriaService.criar(dto)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CriarCategoriaDTO dto) {
        try {
            CategoriaResponseDTO categoria = CategoriaResponseDTO.fromEntity(
                    categoriaService.atualizar(id, dto)
            );
            return ResponseEntity.ok(categoria);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            categoriaService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
