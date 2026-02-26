package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public record AtualizarServicoDTO(
    String titulo,
    String descricao,

    @Positive(message = "Preco deve ser positivo")
    BigDecimal precoBase,

    Boolean disponivel,
    List<String> categorias,

    @Valid
    AtualizarEnderecoDTO endereco
) {
}
