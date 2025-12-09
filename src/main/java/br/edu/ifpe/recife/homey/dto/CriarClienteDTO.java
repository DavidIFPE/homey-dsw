package br.edu.ifpe.recife.homey.dto;

import java.time.LocalDate;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarClienteDTO(
    @NotBlank String nome,
    @NotBlank String email,
    @NotBlank String username,
    @NotBlank String senha,
    @NotNull LocalDate dataNascimento,
    @NotBlank String telefone,
    @NotBlank String cpf
) {
}