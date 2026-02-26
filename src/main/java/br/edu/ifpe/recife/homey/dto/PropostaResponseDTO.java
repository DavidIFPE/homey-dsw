package br.edu.ifpe.recife.homey.dto;

import java.math.BigDecimal;
import java.util.Date;

import br.edu.ifpe.recife.homey.entity.Proposta.StatusProposta;

public class PropostaResponseDTO {
    private Long id;
    private Long contratoId;
    private Long remetenteId;
    private Long destinatarioId;
    private BigDecimal valor;
    private String mensagem;
    private Date prazoResposta;
    private StatusProposta status;
    private Long propostaPaiId;
    private Date dataCriacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContratoId() { return contratoId; }
    public void setContratoId(Long contratoId) { this.contratoId = contratoId; }
    public Long getRemetenteId() { return remetenteId; }
    public void setRemetenteId(Long remetenteId) { this.remetenteId = remetenteId; }
    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Date getPrazoResposta() { return prazoResposta; }
    public void setPrazoResposta(Date prazoResposta) { this.prazoResposta = prazoResposta; }
    public StatusProposta getStatus() { return status; }
    public void setStatus(StatusProposta status) { this.status = status; }
    public Long getPropostaPaiId() { return propostaPaiId; }
    public void setPropostaPaiId(Long propostaPaiId) { this.propostaPaiId = propostaPaiId; }
    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
}
