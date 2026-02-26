package br.edu.ifpe.recife.homey.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record ServicoProximoDTO(
    Long id,
    String titulo,
    String descricao,
    BigDecimal precoBase,
    Boolean disponivel,
    Long prestadorId,
    String prestadorNome,
    List<CategoriaResponseDTO> categorias,
    Date dataCriacao,
    EnderecoResponseDTO endereco,
    double distanciaKm
) {}