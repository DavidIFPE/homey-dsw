package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarCategoriaDTO(
    @NotBlank(message = "Nome da categoria é obrigatório")
    String nome
) {
}
