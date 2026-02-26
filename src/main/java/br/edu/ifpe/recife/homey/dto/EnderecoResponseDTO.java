package br.edu.ifpe.recife.homey.dto;

import br.edu.ifpe.recife.homey.entity.Endereco;

public record EnderecoResponseDTO(
    Long id,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String cep,
    Double latitude,
    Double longitude
) {
    public static EnderecoResponseDTO fromEntity(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoResponseDTO(
            endereco.getId(),
            endereco.getLogradouro(),
            endereco.getNumero(),
            endereco.getComplemento(),
            endereco.getBairro(),
            endereco.getCidade(),
            endereco.getEstado(),
            endereco.getCep(),
            endereco.getLatitude(),
            endereco.getLongitude()
        );
    }
}
