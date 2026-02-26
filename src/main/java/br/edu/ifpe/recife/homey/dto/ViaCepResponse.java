package br.edu.ifpe.recife.homey.dto;

public record ViaCepResponse(
    String cep,
    String logradouro,
    String bairro,
    String localidade,
    String uf,
    Boolean erro
) {}