package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.CriarContratoDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoService {
    
    private final ContratoRepository contratoRepository;
    private final ServicoRepository servicoRepository;

    public ContratoService(ContratoRepository contratoRepository, ServicoRepository servicoRepository) {
        this.contratoRepository = contratoRepository;
        this.servicoRepository = servicoRepository;
    }

    public List<Contrato> listarTodos(Usuario usuarioLogado) {
        if (usuarioLogado instanceof Cliente cliente) {
            return contratoRepository.findAll().stream()
                    .filter(c -> c.getCliente().getId().equals(cliente.getId()))
                    .toList();
        } else {
            // Prestador vê contratos de seus serviços
            return contratoRepository.findAll().stream()
                    .filter(c -> c.getServico().getPrestador().getId().equals(usuarioLogado.getId()))
                    .toList();
        }
    }

    public Contrato buscarPorId(Long id, Usuario usuarioLogado) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        // Verifica se o usuário tem permissão para ver o contrato
        boolean isCliente = contrato.getCliente().getId().equals(usuarioLogado.getId());
        boolean isPrestador = contrato.getServico().getPrestador().getId().equals(usuarioLogado.getId());

        if (!isCliente && !isPrestador) {
            throw new AccessDeniedException("Você não tem permissão para visualizar este contrato");
        }

        return contrato;
    }

    public Contrato criar(CriarContratoDTO dto, Usuario usuarioLogado) {
        if (!(usuarioLogado instanceof Cliente)) {
            throw new AccessDeniedException("Apenas clientes podem criar contratos");
        }

        Servico servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        if (!servico.getDisponivel()) {
            throw new IllegalArgumentException("Este serviço não está disponível");
        }

        if (dto.dataFim().before(dto.dataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        Contrato contrato = new Contrato();
        contrato.setServico(servico);
        contrato.setCliente((Cliente) usuarioLogado);
        contrato.setData_inicio(dto.dataInicio());
        contrato.setData_fim(dto.dataFim());
        contrato.setValor_final(dto.valorFinal());
        contrato.setStatus(Contrato.StatusContrato.PENDENTE);

        return contratoRepository.save(contrato);
    }

    public Contrato atualizarStatus(Long id, Contrato.StatusContrato novoStatus, Usuario usuarioLogado) {
        Contrato contrato = buscarPorId(id, usuarioLogado);

        // Apenas o prestador pode atualizar o status do contrato
        if (!contrato.getServico().getPrestador().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Apenas o prestador pode atualizar o status do contrato");
        }

        contrato.setStatus(novoStatus);
        return contratoRepository.save(contrato);
    }

    public List<Contrato> listarPorCliente(Long clienteId, Usuario usuarioLogado) {
        // Verifica se é o próprio cliente ou o prestador relacionado
        return contratoRepository.findAll().stream()
                .filter(c -> c.getCliente().getId().equals(clienteId))
                .filter(c -> c.getCliente().getId().equals(usuarioLogado.getId()) ||
                           c.getServico().getPrestador().getId().equals(usuarioLogado.getId()))
                .toList();
    }

    public List<Contrato> listarPorPrestador(Long prestadorId, Usuario usuarioLogado) {
        // Verifica se é o próprio prestador
        if (!usuarioLogado.getId().equals(prestadorId)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar estes contratos");
        }

        return contratoRepository.findAll().stream()
                .filter(c -> c.getServico().getPrestador().getId().equals(prestadorId))
                .toList();
    }
}
