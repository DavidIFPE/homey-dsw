package br.edu.ifpe.recife.homey.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "PRESTADOR")
@DiscriminatorValue("P")
@PrimaryKeyJoinColumn(name = "ID_PRESTADOR", referencedColumnName = "ID")
@EqualsAndHashCode(callSuper = false)
public class Prestador extends Usuario implements UserDetails {

    @Column(name = "CPF_CNPJ", nullable = false, unique = true, length = 14)
    private String cpf_cnpj;

    @Column(name = "RESUMO", nullable = true, length = 255)
    private String resumo;

    @Column(name = "AVALIACAO", nullable = true)
    private Double avaliacao;

    @OneToMany(mappedBy = "prestador", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Servico> servicos = new ArrayList<>();

    // Construtor vazio necessário para JPA
    public Prestador() {
    }

    // Getters e Setters
    public String getCpf_cnpj() {
        return cpf_cnpj;
    }

    public void setCpf_cnpj(String cpf_cnpj) {
        this.cpf_cnpj = cpf_cnpj;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    public void addServico(Servico servico) {
        if (this.servicos == null) {
            this.servicos = new ArrayList<>();
        }
        this.servicos.add(servico);
        servico.setPrestador(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRESTADOR"));
    }

    @Override
    public String getPassword() {
        return super.getSenha(); // Corrige o grande erro anterior
    }

    @Override
    public String getUsername() {
        return super.getEmail(); // Email como login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}