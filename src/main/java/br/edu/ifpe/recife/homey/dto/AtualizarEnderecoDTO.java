package br.edu.ifpe.recife.homey.dto;

public record AtualizarEnderecoDTO(
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String cep,
    Double latitude,
    Double longitude
) {
}
