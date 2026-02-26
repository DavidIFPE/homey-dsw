package br.edu.ifpe.recife.homey.dto;

import br.edu.ifpe.recife.homey.entity.Avaliacao;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public record AvaliacaoResponseDTO(
    Long id,
    Integer nota,
    String comentario,
    @JsonProperty("fotoUrl")
    String fotoUrl,
    Long contratoId,
    Long clienteId,
    String clienteNome,
    Long servicoId,
    String servicoTitulo,
    Long prestadorId,
    String prestadorNome,
    Date dataCriacao
) {
    public static AvaliacaoResponseDTO fromEntity(Avaliacao avaliacao) {
        return new AvaliacaoResponseDTO(
            avaliacao.getId(),
            avaliacao.getNota(),
            avaliacao.getComentario(),
            avaliacao.getFotoUrl(),
            avaliacao.getContrato().getId(),
            avaliacao.getCliente().getId(),
            avaliacao.getCliente().getNome(),
            avaliacao.getContrato().getServico().getId(),
            avaliacao.getContrato().getServico().getTitulo(),
            avaliacao.getContrato().getServico().getPrestador().getId(),
            avaliacao.getContrato().getServico().getPrestador().getNome(),
            avaliacao.getDataCriacao()
        );
    }
}
