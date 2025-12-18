package br.edu.ifpe.recife.homey.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.repository.ClienteRepository;
import br.edu.ifpe.recife.homey.repository.PrestadorRepository;

@Service
public class UsuarioService {
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;

    public UsuarioService(ClienteRepository clienteRepository, PrestadorRepository prestadorRepository) {
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
    }

    public Prestador criaPrestador(CriarPrestadorDTO dto) throws Exception {
        Optional<Prestador> usuarioExistente = prestadorRepository.findByEmail(dto.email());

        if(usuarioExistente.isPresent()) throw new Exception("Email já utilizado!");

        Prestador prestador = new Prestador();
        prestador.setCpf_cnpj(dto.cpfCnpj());
        prestador.setNome(dto.nome());
        prestador.setEmail(dto.email());
        prestador.setUsername(dto.username());
        prestador.setTelefone(dto.telefone());
        prestador.setSenha(dto.senha());
        prestador.setDataNascimento(dto.dataNascimento());
        
        prestadorRepository.save(prestador);
        return prestador;
    }

    public Cliente criaCliente(CriarClienteDTO dto) throws Exception {
        Optional<Cliente> usuarioExistente = clienteRepository.findByEmail(dto.email());

        if(usuarioExistente.isPresent()) throw new Exception("Email já utilizado!");

        Cliente cliente = new Cliente();
        cliente.setCpf(dto.cpf());
        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setUsername(dto.username());
        cliente.setTelefone(dto.telefone());
        cliente.setSenha(dto.senha());
        cliente.setDataNascimento(dto.dataNascimento());

        clienteRepository.save(cliente);

        return cliente;
    }
}
