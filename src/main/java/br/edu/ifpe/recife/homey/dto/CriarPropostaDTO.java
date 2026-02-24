package br.edu.ifpe.recife.homey.dto;

import java.math.BigDecimal;
import java.util.Date;

public class CriarPropostaDTO {
    private Long contratoId;
    private Long destinatarioId;
    private BigDecimal valor;
    private Date prazoResposta;
    private String mensagem;
    private Long propostaPaiId; // opcional

    public Long getContratoId() { return contratoId; }
    public void setContratoId(Long contratoId) { this.contratoId = contratoId; }
    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Date getPrazoResposta() { return prazoResposta; }
    public void setPrazoResposta(Date prazoResposta) { this.prazoResposta = prazoResposta; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Long getPropostaPaiId() { return propostaPaiId; }
    public void setPropostaPaiId(Long propostaPaiId) { this.propostaPaiId = propostaPaiId; }
}
