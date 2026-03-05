package br.edu.ifpe.recife.homey.dto;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarPropostaDTO(
    @NotNull
    Long servicoId,
    @Positive
    BigDecimal valor,
    @Future
    Date prazoResposta,
    @Future
    Date dataInicio,
    @Future
    Date dataFim,
    String mensagem
) {}
