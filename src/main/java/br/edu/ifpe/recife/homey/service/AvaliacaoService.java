package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.CriarAvaliacaoDTO;
import br.edu.ifpe.recife.homey.entity.Avaliacao;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.AvaliacaoRepository;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AvaliacaoService {
    
    private final AvaliacaoRepository avaliacaoRepository;
    private final ContratoRepository contratoRepository;
    private final FotoService fotoService;
    
    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, 
                            ContratoRepository contratoRepository,
                            FotoService fotoService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.contratoRepository = contratoRepository;
        this.fotoService = fotoService;
    }
    
    @Transactional
    public Avaliacao criar(CriarAvaliacaoDTO dto, MultipartFile foto, Usuario usuarioLogado) {
        // Verifica se o usuário é cliente
        if (!(usuarioLogado instanceof Cliente cliente)) {
            throw new AccessDeniedException("Apenas clientes podem criar avaliações");
        }
        
        // Busca o contrato
        Contrato contrato = contratoRepository.findById(dto.contratoId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
        
        // Verifica se o cliente é o proprietário do contrato
        if (!contrato.getCliente().getId().equals(cliente.getId())) {
            throw new AccessDeniedException("Você não tem permissão para avaliar este contrato");
        }
        
        // Verifica se o contrato está concluído
        if (!contrato.getStatus().equals(Contrato.StatusContrato.CONCLUIDO)) {
            throw new IllegalArgumentException("Apenas contratos concluídos podem ser avaliados");
        }
        
        // Verifica se já existe avaliação para este contrato
        if (avaliacaoRepository.findByContratoId(dto.contratoId()).isPresent()) {
            throw new IllegalArgumentException("Este contrato já possui uma avaliação");
        }
        
        // Salva a foto se fornecida
        String fotoUrl = null;
        if (foto != null && !foto.isEmpty()) {
            try {
                fotoUrl = fotoService.salvarFoto(foto, cliente.getId());
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar foto: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Validação de foto falhou: " + e.getMessage());
            }
        }
        
        // Cria a avaliação
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());
        avaliacao.setFotoUrl(fotoUrl);
        avaliacao.setContrato(contrato);
        avaliacao.setCliente(cliente);
        
        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);
        
        return avaliacaoSalva;
    }
    
    public Avaliacao buscarPorContrato(Long contratoId, Usuario usuarioLogado) {
        Avaliacao avaliacao = avaliacaoRepository.findByContratoId(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        
        // Verifica permissão: cliente, prestador do serviço ou admin
        boolean isClienteDoContrato = avaliacao.getCliente().getId().equals(usuarioLogado.getId());
        boolean isPrestadorDoServico = avaliacao.getContrato().getServico().getPrestador().getId().equals(usuarioLogado.getId());
        
        if (!isClienteDoContrato && !isPrestadorDoServico) {
            throw new AccessDeniedException("Você não tem permissão para visualizar esta avaliação");
        }
        
        return avaliacao;
    }
    
    public List<Avaliacao> listarPorCliente(Long clienteId, Usuario usuarioLogado) {
        // Verifica se o usuário tem permissão (cliente vê suas próprias avaliações)
        if (usuarioLogado instanceof Cliente cliente) {
            if (!cliente.getId().equals(clienteId)) {
                throw new AccessDeniedException("Você não tem permissão para listar avaliações de outro cliente");
            }
        } else {
            // Prestador pode ver avaliações do cliente
            throw new AccessDeniedException("Apenas clientes podem listar suas avaliações desta forma");
        }
        
        return avaliacaoRepository.findByClienteId(clienteId);
    }
    
    public List<Avaliacao> listarPorPrestador(Long prestadorId, Usuario usuarioLogado) {
        // Verifica se o usuário é o prestador ou tem permissão
        if (!usuarioLogado.getId().equals(prestadorId)) {
            throw new AccessDeniedException("Você não tem permissão para listar avaliações de outro prestador");
        }
        
        return avaliacaoRepository.findByContrato_Servico_PrestadorId(prestadorId);
    }
    
    public void deletar(Long avaliacaoId, Usuario usuarioLogado) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrado"));
        
        // Apenas o cliente que fez a avaliação pode deletar
        if (!avaliacao.getCliente().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar esta avaliação");
        }
        
        // Deleta a foto se existir
        if (avaliacao.getFotoUrl() != null) {
            fotoService.deletarFoto(avaliacao.getFotoUrl());
        }
        
        avaliacaoRepository.deleteById(avaliacaoId);
    }
}
