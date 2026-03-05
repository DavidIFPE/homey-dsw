package br.edu.ifpe.recife.homey.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.edu.ifpe.recife.homey.entity.Proposta.StatusProposta;
import jakarta.persistence.*;

@Entity
@Table(name = "CONTRATO")
public class Contrato {
    
    public enum StatusContrato {
        PENDENTE, ATIVO, CONCLUIDO, CANCELADO
    }
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "DT_INICIO", nullable = true)
    private Date data_inicio;
    
    @Column(name = "DT_FIM", nullable = true)
    private Date data_fim;
    
    @Column(name = "VALOR_FINAL", precision = 10, scale = 2)
    private BigDecimal valor_final;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Avaliacao> avaliacoes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusContrato status = StatusContrato.PENDENTE;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ID_SERVICO", referencedColumnName = "ID")
    private Servico servico;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ID_CLIENTE", referencedColumnName = "ID")
    private Cliente cliente;
    
    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proposta> propostas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROPOSTA_ACEITA")
    private Proposta propostaAceita;
    
    @Column(name = "DT_CRIACAO")
    protected Date dataCriacao;

    public Contrato() {
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

    public Date getData_inicio() {
        return data_inicio;
    }

    public void setData_inicio(Date data_inicio) {
        this.data_inicio = data_inicio;
    }

    public Date getData_fim() {
        return data_fim;
    }

    public void setData_fim(Date data_fim) {
        this.data_fim = data_fim;
    }

    public BigDecimal getValor_final() {
        return valor_final;
    }

    public void setValor_final(BigDecimal valor_final) {
        this.valor_final = valor_final;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void addProposta(Proposta proposta) {
        if (this.propostas == null) {
            this.propostas = new ArrayList<>();
        }
        this.propostas.forEach(p -> { p.setStatus(StatusProposta.RECUSADA); });
        this.propostas.add(proposta);
        proposta.setContrato(this);
    }

    public List<Proposta> getPropostas() {
        return propostas;
    }

    public void setPropostas(List<Proposta> propostas) {
        this.propostas = propostas;
    }

    public Proposta getPropostaAceita() {
        return propostaAceita;
    }

    public void setPropostaAceita(Proposta propostaAceita) {
        this.propostaAceita = propostaAceita;
        if (propostaAceita != null) {
            this.valor_final = propostaAceita.getValor();
            this.status = StatusContrato.ATIVO;
        }
    }

    public StatusContrato getStatus() {
        return status;
    }

    public void setStatus(StatusContrato status) {
        this.status = status;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }
}