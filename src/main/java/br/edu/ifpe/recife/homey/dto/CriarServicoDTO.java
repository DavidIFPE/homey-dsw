package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CriarServicoDTO(
    @NotBlank(message = "Título é obrigatório")
    String titulo,
    
    String descricao,
    
    @NotNull(message = "Preço base é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    BigDecimal precoBase,
    
    @NotNull(message = "Disponibilidade é obrigatória")
    Boolean disponivel,
    
    List<String> categorias
) {
}
