package br.edu.ifpe.recife.homey.factory;

import java.math.BigDecimal;
import java.util.Date;

import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.entity.Usuario;

public class PropostaFactory {
    public static Proposta criarPendente(Long id,
                                         Usuario remetente,
                                         Usuario destinatario,
                                         Contrato contrato,
                                         BigDecimal valor) {
        Proposta p = new Proposta();
        p.setId(id);
        p.setRemetente(remetente);
        p.setDestinatario(destinatario);
        p.setContrato(contrato);
        p.setValor(valor);
        p.setStatus(Proposta.StatusProposta.PENDENTE);
        p.setPrazoResposta(new Date(System.currentTimeMillis() + 48 * 3600_000L));
        return p;
    }
}
