package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record RegistroDTO(
    @NotBlank String nome,
    @NotBlank String email,
    @NotBlank String senha,
    @NotBlank String tipo,
    LocalDate dataNascimento,
    String telefone,
    String cpf,
    String cpfCnpj
) {
}
