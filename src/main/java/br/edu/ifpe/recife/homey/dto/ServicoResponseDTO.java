package br.edu.ifpe.recife.homey.dto;

import br.edu.ifpe.recife.homey.entity.Servico;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public record ServicoResponseDTO(
    Long id,
    String titulo,
    String descricao,
    BigDecimal precoBase,
    Boolean disponivel,
    Long prestadorId,
    String prestadorNome,
    List<CategoriaResponseDTO> categorias,
    Date dataCriacao
) {
    public static ServicoResponseDTO fromEntity(Servico servico) {
        return new ServicoResponseDTO(
            servico.getId(),
            servico.getTitulo(),
            servico.getDescricao(),
            servico.getPrecoBase(),
            servico.getDisponivel(),
            servico.getPrestador().getId(),
            servico.getPrestador().getNome(),
            servico.getCategorias() != null ? 
                servico.getCategorias().stream()
                    .map(CategoriaResponseDTO::fromEntity)
                    .collect(Collectors.toList()) : 
                List.of(),
            servico.getDataCriacao()
        );
    }
}
