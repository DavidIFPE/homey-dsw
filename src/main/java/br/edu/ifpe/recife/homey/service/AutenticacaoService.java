package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.RegistroDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username)
                .map(usuario -> (UserDetails) usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
    }

    public Usuario registrarUsuario(RegistroDTO data) {
        if (repository.findByEmail(data.email()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        String encryptedPassword = passwordEncoder.encode(data.senha());

        if ("CLIENTE".equalsIgnoreCase(data.tipo())) {
            return criarCliente(data, encryptedPassword);
        } else if ("PRESTADOR".equalsIgnoreCase(data.tipo())) {
            return criarPrestador(data, encryptedPassword);
        } else {
            throw new IllegalArgumentException("Tipo de usuário inválido. Use 'CLIENTE' ou 'PRESTADOR'");
        }
    }

    private Cliente criarCliente(RegistroDTO data, String encryptedPassword) {
        Cliente cliente = new Cliente();
        cliente.setNome(data.nome());
        cliente.setEmail(data.email());
        cliente.setUsername(data.email());
        cliente.setSenha(encryptedPassword);
        cliente.setDataNascimento(data.dataNascimento());
        cliente.setTelefone(data.telefone());
        cliente.setCpf(data.cpf());
        
        return repository.save(cliente);
    }

    private Prestador criarPrestador(RegistroDTO data, String encryptedPassword) {
        Prestador prestador = new Prestador();
        prestador.setNome(data.nome());
        prestador.setEmail(data.email());
        prestador.setUsername(data.email());
        prestador.setSenha(encryptedPassword);
        prestador.setDataNascimento(data.dataNascimento());
        prestador.setTelefone(data.telefone());
        prestador.setCpf_cnpj(data.cpfCnpj());
        
        return repository.save(prestador);
    }
}