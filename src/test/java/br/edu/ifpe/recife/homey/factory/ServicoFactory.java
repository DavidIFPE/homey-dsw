package br.edu.ifpe.recife.homey.factory;

import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Servico;

import java.math.BigDecimal;
import java.util.List;

public final class ServicoFactory {

    private ServicoFactory() {
    }

    public static Servico criarServicoValido(Long id, Prestador prestador, Endereco endereco, List<Categoria> categorias) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setTitulo("Serviço Teste");
        servico.setDescricao("Descrição do serviço de teste");
        servico.setPrecoBase(BigDecimal.valueOf(100.0));
        servico.setDisponivel(true);
        servico.setPrestador(prestador);
        servico.setEndereco(endereco);
        servico.setCategorias(categorias);
        return servico;
    }
}
