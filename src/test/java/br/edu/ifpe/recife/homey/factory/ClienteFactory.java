package br.edu.ifpe.recife.homey.factory;

import br.edu.ifpe.recife.homey.entity.Cliente;

public final class ClienteFactory {

    private ClienteFactory() {
    }

    public static Cliente criarClienteValido(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@test.com");
        cliente.setUsername("cliente.test");
        cliente.setSenha("senha-segura");
        cliente.setCpf("12345678901");
        return cliente;
    }
}
