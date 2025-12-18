package br.edu.ifpe.recife.homey.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "CLIENTE")
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "ID_CLIENTE", referencedColumnName = "ID")
@EqualsAndHashCode(callSuper = false)
public class Cliente extends Usuario implements UserDetails {

    @Column(name = "CPF", nullable = true, unique = true, length = 11)
    private String cpf;

    // Construtor vazio necessário para JPA
    public Cliente() {
    }

    // Construtor personalizado usado no registro (só email e senha)
    public Cliente(String email, String senha) {
        this.setEmail(email);
        this.setSenha(senha);
        this.setUsername(email); // usa email como username (campo obrigatório)
        this.setNome(email); // usa email como nome inicial
        // dataNascimento, telefone e cpf podem ser preenchidos posteriormente
    }

    // Getters e Setters
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return super.getSenha();
    }

    @Override
    public String getUsername() {
        return super.getEmail();
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