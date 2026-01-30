package br.edu.ifpe.recife.homey.dto;

import br.edu.ifpe.recife.homey.entity.Categoria;

import java.util.Date;

public record CategoriaResponseDTO(
    Long id,
    String nome,
    Date dataCriacao
) {
    public static CategoriaResponseDTO fromEntity(Categoria categoria) {
        return new CategoriaResponseDTO(
            categoria.getId(),
            categoria.getNome(),
            categoria.getDataCriacao()
        );
    }
}
