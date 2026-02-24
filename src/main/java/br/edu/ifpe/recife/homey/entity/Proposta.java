package br.edu.ifpe.recife.homey.entity;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROPOSTA")
public class Proposta {

    public enum StatusProposta {
        PENDENTE, ACEITA, RECUSADA, CONTRAPROPOSTA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONTRATO", referencedColumnName = "ID")
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REMENTENTE", referencedColumnName = "ID")
    private Usuario remetente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DESTINATARIO", referencedColumnName = "ID")
    private Usuario destinatario;

    @Column(name = "VALOR", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "MENSAGEM", length = 8000)
    private String mensagem;

    @Column(name = "PRAZO_RESPOSTA")
    private Date prazoResposta;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusProposta status = StatusProposta.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROPOSTA_PAI")
    private Proposta propostaPai;

    @Column(name = "DT_CRIACAO")
    protected Date dataCriacao;

    public Proposta() {
    }

    @PrePersist
    public void setDataCriacao() {
        this.dataCriacao = new Date();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public Usuario getRemetente() {
        return remetente;
    }

    public void setRemetente(Usuario remetente) {
        this.remetente = remetente;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Date getPrazoResposta() {
        return prazoResposta;
    }

    public void setPrazoResposta(Date prazoResposta) {
        this.prazoResposta = prazoResposta;
    }

    public StatusProposta getStatus() {
        return status;
    }

    public void setStatus(StatusProposta status) {
        this.status = status;
    }

    public Proposta getPropostaPai() {
        return propostaPai;
    }

    public void setPropostaPai(Proposta propostaPai) {
        this.propostaPai = propostaPai;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }
}
