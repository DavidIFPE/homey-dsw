package br.edu.ifpe.recife.homey.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "AVALIACAO")
public class Avaliacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "NOTA", nullable = false)
    private Integer nota;
    
    @Column(name = "COMENTARIO", nullable = true, length = 500)
    private String comentario;
    
    @Column(name = "FOTO_URL", nullable = true, length = 500)
    private String fotoUrl;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ID_CONTRATO", referencedColumnName = "ID")
    private Contrato contrato;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ID_CLIENTE", referencedColumnName = "ID")
    private Cliente cliente;
    
    @Column(name = "DT_CRIACAO")
    protected Date dataCriacao;
    
    public Avaliacao() {
    }
    
    public Avaliacao(Integer nota, String comentario, String fotoUrl, Contrato contrato, Cliente cliente) {
        this.nota = nota;
        this.comentario = comentario;
        this.fotoUrl = fotoUrl;
        this.contrato = contrato;
        this.cliente = cliente;
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
    
    public Integer getNota() {
        return nota;
    }
    
    public void setNota(Integer nota) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("Nota deve estar entre 1 e 5");
        }
        this.nota = nota;
    }
    
    public String getComentario() {
        return comentario;
    }
    
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    
    public String getFotoUrl() {
        return fotoUrl;
    }
    
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
    
    public Contrato getContrato() {
        return contrato;
    }
    
    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Date getDataCriacao() {
        return dataCriacao;
    }
}
