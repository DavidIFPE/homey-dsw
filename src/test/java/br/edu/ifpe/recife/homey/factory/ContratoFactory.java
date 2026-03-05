package br.edu.ifpe.recife.homey.factory;

import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Servico;

public class ContratoFactory {
    public static Contrato criarContratoPendente(Long id, Servico servico, Cliente cliente) {
        Contrato c = new Contrato();
        c.setId(id);
        c.setServico(servico);
        c.setCliente(cliente);
        c.setStatus(Contrato.StatusContrato.PENDENTE);
        c.setPropostas(new java.util.ArrayList<>());
        return c;
    }
}
