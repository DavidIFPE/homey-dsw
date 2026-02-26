package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.*;

public record CriarAvaliacaoDTO(
    @NotNull(message = "ID do contrato é obrigatório")
    Long contratoId,
    
    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota mínima é 1")
    @Max(value = 5, message = "Nota máxima é 5")
    Integer nota,
    
    @Size(max = 500, message = "Comentário não pode exceder 500 caracteres")
    String comentario
) {
}
