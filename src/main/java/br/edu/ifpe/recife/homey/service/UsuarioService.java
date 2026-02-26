package br.edu.ifpe.recife.homey.service;

import java.io.IOException;
import java.util.Optional;

import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.repository.ClienteRepository;
import br.edu.ifpe.recife.homey.repository.PrestadorRepository;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final FotoService fotoService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(ClienteRepository clienteRepository, PrestadorRepository prestadorRepository, UsuarioRepository usuarioRepository, FotoService fotoService) {
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotoService = fotoService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String uploadFoto(Usuario usuario, MultipartFile foto) throws IOException, IOException {
        if (usuario.getFotoUrl() != null) {
            fotoService.deletarFoto(usuario.getFotoUrl());
        }

        String fotoUrl = fotoService.salvarFoto(foto, usuario.getId());

        usuario.setFotoUrl(fotoUrl);
        usuarioRepository.save(usuario);

        return fotoUrl;
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
        prestador.setSenha(passwordEncoder.encode(dto.senha()));
        prestador.setDataNascimento(dto.dataNascimento());

        if (dto.endereco() != null) {
            prestador.setEndereco(mapearEndereco(dto.endereco()));
        }
        
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
        cliente.setSenha(passwordEncoder.encode(dto.senha()));
        cliente.setDataNascimento(dto.dataNascimento());

        if (dto.endereco() != null) {
            cliente.setEndereco(mapearEndereco(dto.endereco()));
        }

        clienteRepository.save(cliente);

        return cliente;
    }

    public Prestador pegarPrestador(Long id) {
        return prestadorRepository.findById(id).orElse(null);
    }

    private Endereco mapearEndereco(EnderecoDTO dto) {
        Endereco endereco = new Endereco();
        endereco.setLogradouro(dto.logradouro());
        endereco.setNumero(dto.numero());
        endereco.setComplemento(dto.complemento());
        endereco.setBairro(dto.bairro());
        endereco.setCidade(dto.cidade());
        endereco.setEstado(dto.estado());
        endereco.setCep(dto.cep());
        endereco.setLatitude(dto.latitude());
        endereco.setLongitude(dto.longitude());
        return endereco;
    }
}
