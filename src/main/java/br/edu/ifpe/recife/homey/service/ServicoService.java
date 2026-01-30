package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.AtualizarServicoDTO;
import br.edu.ifpe.recife.homey.dto.CriarServicoDTO;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicoService {
    
    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;

    public ServicoService(ServicoRepository servicoRepository, CategoriaRepository categoriaRepository) {
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    public List<Servico> listarPorPrestador(Long prestadorId) {
        return servicoRepository.findAll().stream()
                .filter(s -> s.getPrestador().getId().equals(prestadorId))
                .toList();
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
}
