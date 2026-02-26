package br.edu.ifpe.recife.homey.factory;

import br.edu.ifpe.recife.homey.entity.Prestador;

public final class PrestadorFactory {

    private PrestadorFactory() {
    }

    public static Prestador criarPrestadorValido(Long id) {
        Prestador prestador = new Prestador();
        prestador.setId(id);
        prestador.setNome("Prestador Teste");
        prestador.setEmail("prestador@test.com");
        prestador.setUsername("prestador.test");
        prestador.setSenha("senha-segura");
        prestador.setCpf_cnpj("12345678901234");
        prestador.setResumo("Prestador de serviços de teste");
        prestador.setAvaliacao(5.0);
        return prestador;
    }
}
