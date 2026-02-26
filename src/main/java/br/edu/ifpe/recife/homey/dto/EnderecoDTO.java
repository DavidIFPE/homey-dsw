package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.NotNull;

public record EnderecoDTO(
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String cep,
    @NotNull(message = "Latitude é obrigatória")
    Double latitude,
    @NotNull(message = "Longitude é obrigatória")
    Double longitude
) {
}
