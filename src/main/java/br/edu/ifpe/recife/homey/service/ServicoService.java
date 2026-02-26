package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.AtualizarServicoDTO;
import br.edu.ifpe.recife.homey.dto.CategoriaResponseDTO;
import br.edu.ifpe.recife.homey.dto.CriarServicoDTO;
import br.edu.ifpe.recife.homey.dto.AtualizarEnderecoDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoResponseDTO;
import br.edu.ifpe.recife.homey.dto.ServicoProximoDTO;
import br.edu.ifpe.recife.homey.dto.ServicoResponseDTO;
import br.edu.ifpe.recife.homey.dto.ViaCepResponse;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Coordenada;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ServicoService {
    
    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final NominatimService nominatimService;
    private final ViaCepService viaCepService;
    private final DistanceService distanceService;

    public ServicoService(ServicoRepository servicoRepository, CategoriaRepository categoriaRepository, NominatimService nominatimService, ViaCepService viaCepService, DistanceService distanceService) {
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.nominatimService = nominatimService;
        this.viaCepService = viaCepService;
        this.distanceService = distanceService;
    }

    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    public List<Servico> listarPorPrestador(Long prestadorId) {
        return servicoRepository.findAll().stream()
                .filter(s -> s.getPrestador().getId().equals(prestadorId))
                .toList();
    }

    public List<Servico> listarPorCategoriaNome(String categoriaNome) {
        if (categoriaNome == null) {
            return List.of();
        }
        String nome = categoriaNome.trim();
        if (nome.isEmpty()) {
            return List.of();
        }

        return servicoRepository.findByCategorias_NomeIgnoreCase(nome);
    }

        public List<ServicoProximoDTO> buscarPorCep(String cep) {
        return buscarPorCep(cep, null);
        }

        public List<ServicoProximoDTO> buscarPorCep(String cep, String categoriaNome) {
        // CEP do usuário → endereço textual
        ViaCepResponse viaCep = viaCepService.buscarEnderecoPorCep(cep);
        String enderecoUsuario =
            viaCepService.montarEnderecoCompleto(viaCep);

        // Endereço → coordenadas do usuário
        Coordenada coordenadaUsuario =
            nominatimService.obterCoordenadas(enderecoUsuario);

        // Buscar serviços disponíveis
        List<Servico> servicos =
            servicoRepository.findByDisponivelTrue();

        // Filtrar por categoria, se informado
        if (categoriaNome != null) {
            String nome = categoriaNome.trim();
            if (!nome.isEmpty()) {
            String nomeFinal = nome;
            servicos = servicos.stream()
                .filter(s -> s.getCategorias() != null &&
                    s.getCategorias().stream()
                        .anyMatch(c -> c.getNome() != null && c.getNome().equalsIgnoreCase(nomeFinal)))
                .toList();
            }
        }

        // Calcular distância usando ENDERECO do serviço
        return servicos.stream()
                .filter(s ->
                        s.getEndereco() != null &&
                        s.getEndereco().getLatitude() != null &&
                        s.getEndereco().getLongitude() != null
                )
                .map(servico -> {

                    Endereco enderecoServico = servico.getEndereco();

                    double distanciaKm =
                            distanceService.calcularKm(
                                    coordenadaUsuario.latitude(),
                                    coordenadaUsuario.longitude(),
                                    enderecoServico.getLatitude(),
                                    enderecoServico.getLongitude()
                            );

                    return new ServicoProximoDTO(
                            servico.getId(),
                            servico.getTitulo(),
                            servico.getDescricao(),
                            servico.getPrecoBase(),
                            servico.getDisponivel(),
                            servico.getPrestador().getId(),
                            servico.getPrestador().getNome(),
                            servico.getCategorias() != null ?
                                    servico.getCategorias(). stream()
                                            .map(CategoriaResponseDTO::fromEntity)
                                            .toList() :
                                    List.of(),
                            servico.getDataCriacao(),
                            EnderecoResponseDTO.fromEntity(enderecoServico),
                            arredondar(distanciaKm)
                    );
                })
                .sorted(Comparator.comparingDouble(ServicoProximoDTO::distanciaKm))
                .toList();
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public Servico buscarPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
    }

    public Servico criar(CriarServicoDTO dto, Usuario usuarioLogado) {
        if (!(usuarioLogado instanceof Prestador)) {
            throw new AccessDeniedException("Apenas prestadores podem criar serviços");
        }

        Servico servico = new Servico();
        servico.setTitulo(dto.titulo());
        servico.setDescricao(dto.descricao());
        servico.setPrecoBase(dto.precoBase());
        servico.setDisponivel(dto.disponivel());
        servico.setPrestador((Prestador) usuarioLogado);

        servico.setEndereco(mapearEndereco(dto.endereco()));

        servico.setCategorias(resolverCategoriasPorNome(dto.categorias()));

        return servicoRepository.save(servico);
    }

    public Servico atualizar(Long id, AtualizarServicoDTO dto, Usuario usuarioLogado) {
        Servico servico = buscarPorId(id);

        if (!servico.getPrestador().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Você não tem permissão para atualizar este serviço");
        }

        if (dto.titulo() != null) servico.setTitulo(dto.titulo());
        if (dto.descricao() != null) servico.setDescricao(dto.descricao());
        if (dto.precoBase() != null) servico.setPrecoBase(dto.precoBase());
        if (dto.disponivel() != null) servico.setDisponivel(dto.disponivel());

        if (dto.categorias() != null) {
            servico.setCategorias(resolverCategoriasPorNome(dto.categorias()));
        }

        if (dto.endereco() != null) {
            atualizarEndereco(servico, dto.endereco());
        }

        return servicoRepository.save(servico);
    }

    private List<Categoria> resolverCategoriasPorNome(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Categoria> categorias = new ArrayList<>();

        for (String nomeRaw : nomes) {
            if (nomeRaw == null) {
                continue;
            }

            String nome = nomeRaw.trim();
            if (nome.isEmpty()) {
                continue;
            }

            Categoria categoria = categoriaRepository.findByNomeIgnoreCase(nome)
                    .orElseGet(() -> {
                        Categoria nova = new Categoria();
                        nova.setNome(nome);
                        return categoriaRepository.save(nova);
                    });

            categorias.add(categoria);
        }

        return categorias;
    }

    public void deletar(Long id, Usuario usuarioLogado) {
        Servico servico = buscarPorId(id);

        if (!servico.getPrestador().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar este serviço");
        }

        servicoRepository.delete(servico);
    }

    public Servico alterarDisponibilidade(Long id, Boolean disponivel, Usuario usuarioLogado) {
        Servico servico = buscarPorId(id);

        if (!servico.getPrestador().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Você não tem permissão para alterar este serviço");
        }

        servico.setDisponivel(disponivel);
        return servicoRepository.save(servico);
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

    private void atualizarEndereco(Servico servico, AtualizarEnderecoDTO dto) {
        Endereco endereco = servico.getEndereco();
        if (endereco == null) {
            endereco = new Endereco();
            servico.setEndereco(endereco);
        }

        if (dto.logradouro() != null) endereco.setLogradouro(dto.logradouro());
        if (dto.numero() != null) endereco.setNumero(dto.numero());
        if (dto.complemento() != null) endereco.setComplemento(dto.complemento());
        if (dto.bairro() != null) endereco.setBairro(dto.bairro());
        if (dto.cidade() != null) endereco.setCidade(dto.cidade());
        if (dto.estado() != null) endereco.setEstado(dto.estado());
        if (dto.cep() != null) endereco.setCep(dto.cep());
        if (dto.latitude() != null) endereco.setLatitude(dto.latitude());
        if (dto.longitude() != null) endereco.setLongitude(dto.longitude());
    }
}
