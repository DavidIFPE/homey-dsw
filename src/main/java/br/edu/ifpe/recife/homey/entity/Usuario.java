package br.edu.ifpe.recife.homey.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "USUARIO")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@DiscriminatorColumn(name = "DISC_USUARIO", discriminatorType = DiscriminatorType.STRING, length = 1)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    protected String nome;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    protected String email;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 30)
    protected String username;

    @JsonIgnore
    @Column(name = "SENHA", nullable = false, length = 255)
    protected String senha;

    @Column(name = "DT_NASCIMENTO", nullable = true)
    protected LocalDate dataNascimento;

    @Column(name = "TELEFONE", nullable = true, length = 20)
    protected String telefone;

    @Column(name = "DT_CRIACAO")
    protected Date dataCriacao;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ENDERECO", referencedColumnName = "ID", nullable = true)
    private Endereco endereco;

    @Column(name = "FOTO_URL", length = 500)
    protected String fotoUrl;

    @PrePersist
    public void setDataCriacao() {
        this.dataCriacao = new Date();
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}