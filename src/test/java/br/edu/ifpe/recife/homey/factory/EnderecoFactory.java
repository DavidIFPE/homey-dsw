package br.edu.ifpe.recife.homey.factory;

import br.edu.ifpe.recife.homey.entity.Endereco;

public final class EnderecoFactory {

    private EnderecoFactory() {
    }

    public static Endereco criarEnderecoValido(Long id) {
        Endereco endereco = new Endereco();
        endereco.setId(id);
        endereco.setLogradouro("Rua Teste");
        endereco.setNumero("123");
        endereco.setComplemento("Apto 1");
        endereco.setBairro("Bairro Teste");
        endereco.setCidade("Recife");
        endereco.setEstado("PE");
        endereco.setCep("50000000");
        endereco.setLatitude(-8.0476);
        endereco.setLongitude(-34.8770);
        return endereco;
    }
}
