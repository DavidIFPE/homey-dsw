package br.edu.ifpe.recife.homey.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Date;

public record CriarContratoDTO(
    @NotNull(message = "ID do serviço é obrigatório")
    Long servicoId,
    
    @NotNull(message = "Data de início é obrigatória")
    Date dataInicio,
    
    @NotNull(message = "Data de fim é obrigatória")
    Date dataFim,
    
    @NotNull(message = "Valor final é obrigatório")
    BigDecimal valorFinal
) {
}
