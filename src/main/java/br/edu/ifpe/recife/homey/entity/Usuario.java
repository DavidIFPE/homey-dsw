package br.edu.ifpe.recife.homey.entity;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "USUARIO")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DISC_USUARIO",
        discriminatorType = DiscriminatorType.STRING, length = 1)
@Data
public abstract class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "NOME", nullable = false, length = 100)
    protected String nome;
    
    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    protected String email;
    
    @Column(name = "USERNAME", nullable = false, unique = true, length = 30)
    protected String username;
    
    @Column(name = "SENHA", nullable = false, length = 255)
    protected String senha;
    
    @Column(name = "DT_NASCIMENTO", nullable = false)
    protected LocalDate dataNascimento;
    
    @Column(name = "TELEFONE", nullable = false, length = 20)
    protected String telefone;
    
    @Column(name = "DT_CRIACAO")
    protected Date dataCriacao;    
    
    @PrePersist
    public void setDataCriacao() {
        this.dataCriacao = new Date();
    }

}