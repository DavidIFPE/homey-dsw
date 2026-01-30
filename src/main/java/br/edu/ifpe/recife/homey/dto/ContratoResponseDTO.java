package br.edu.ifpe.recife.homey.dto;

import br.edu.ifpe.recife.homey.entity.Contrato;

import java.math.BigDecimal;
import java.util.Date;

public record ContratoResponseDTO(
    Long id,
    Date dataInicio,
    Date dataFim,
    BigDecimal valorFinal,
    String status,
    Long servicoId,
    String servicoTitulo,
    Long clienteId,
    String clienteNome,
    Long prestadorId,
    String prestadorNome,
    Date dataCriacao
) {
    public static ContratoResponseDTO fromEntity(Contrato contrato) {
        return new ContratoResponseDTO(
            contrato.getId(),
            contrato.getData_inicio(),
            contrato.getData_fim(),
            contrato.getValor_final(),
            contrato.getStatus().name(),
            contrato.getServico().getId(),
            contrato.getServico().getTitulo(),
            contrato.getCliente().getId(),
            contrato.getCliente().getNome(),
            contrato.getServico().getPrestador().getId(),
            contrato.getServico().getPrestador().getNome(),
            contrato.getDataCriacao()
        );
    }
}
