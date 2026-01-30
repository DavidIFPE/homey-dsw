package br.edu.ifpe.recife.homey.dto;

import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Usuario;

import java.time.LocalDate;
import java.util.Date;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    String username,
    LocalDate dataNascimento,
    String telefone,
    String tipo,
    String cpf,
    String cpfCnpj,
    String resumo,
    Double avaliacao,
    Date dataCriacao
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        if (usuario instanceof Cliente cliente) {
            return new UsuarioResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getUsername(),
                cliente.getDataNascimento(),
                cliente.getTelefone(),
                "CLIENTE",
                cliente.getCpf(),
                null,
                null,
                null,
                cliente.getDataCriacao()
            );
        } else if (usuario instanceof Prestador prestador) {
            return new UsuarioResponseDTO(
                prestador.getId(),
                prestador.getNome(),
                prestador.getEmail(),
                prestador.getUsername(),
                prestador.getDataNascimento(),
                prestador.getTelefone(),
                "PRESTADOR",
                null,
                prestador.getCpf_cnpj(),
                prestador.getResumo(),
                prestador.getAvaliacao(),
                prestador.getDataCriacao()
            );
        }
        return null;
    }
}
