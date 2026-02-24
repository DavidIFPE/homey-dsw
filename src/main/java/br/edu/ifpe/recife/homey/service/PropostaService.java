package br.edu.ifpe.recife.homey.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpe.recife.homey.dto.CriarPropostaDTO;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.entity.Proposta.StatusProposta;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import br.edu.ifpe.recife.homey.repository.PropostaRepository;
import br.edu.ifpe.recife.homey.repository.UsuarioRepository;

@Service
public class PropostaService {

    private final PropostaRepository propostaRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;

    public PropostaService(PropostaRepository propostaRepository,
                          ContratoRepository contratoRepository,
                          UsuarioRepository usuarioRepository) {
        this.propostaRepository = propostaRepository;
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Proposta criarProposta(CriarPropostaDTO dto) {
        Contrato contrato = contratoRepository.findById(dto.getContratoId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
        // obter usuário autenticado e usar como remetente
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        Usuario usuarioAutenticado = (Usuario) principal;

        Usuario remetente = usuarioRepository.findById(usuarioAutenticado.getId())
            .orElseThrow(() -> new IllegalArgumentException("Remetente não encontrado"));

        Usuario destinatario = usuarioRepository.findById(dto.getDestinatarioId())
            .orElseThrow(() -> new IllegalArgumentException("Destinatário não encontrado"));

        Proposta p = new Proposta();
        p.setContrato(contrato);
        p.setRemetente(remetente);
        p.setDestinatario(destinatario);
        p.setValor(dto.getValor());
        p.setPrazoResposta(dto.getPrazoResposta());
        p.setMensagem(dto.getMensagem());
        p.setStatus(StatusProposta.PENDENTE);
        if (dto.getPropostaPaiId() != null) {
            propostaRepository.findById(dto.getPropostaPaiId()).ifPresent(p::setPropostaPai);
        }

        Proposta salva = propostaRepository.save(p);
        contrato.addProposta(salva);
        contratoRepository.save(contrato);
        return salva;
    }

    public List<Proposta> listarHistorico(Contrato contrato) {
        return propostaRepository.findByContratoOrderByDataCriacaoAsc(contrato);
    }

    @Transactional
    public Proposta aceitarProposta(Long propostaId) {
        Proposta p = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta não encontrada"));
        if (p.getStatus() != StatusProposta.PENDENTE) {
            throw new IllegalStateException("Somente propostas pendentes podem ser aceitas");
        }
        // somente o destinatário pode aceitar
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        Usuario usuarioAutenticado = (Usuario) principal;
        if (p.getDestinatario() == null || !usuarioAutenticado.getId().equals(p.getDestinatario().getId())) {
            throw new AccessDeniedException("Somente o destinatário pode aceitar a proposta");
        }

        p.setStatus(StatusProposta.ACEITA);

        Contrato contrato = p.getContrato();
        contrato.setValor_final(p.getValor());
        contrato.setStatus(Contrato.StatusContrato.ATIVO);
        contratoRepository.save(contrato);

        if (contrato.getPropostas() != null) {
            contrato.getPropostas().stream()
                    .filter(pp -> !pp.getId().equals(p.getId()) && pp.getStatus() == StatusProposta.PENDENTE)
                    .forEach(pp -> pp.setStatus(StatusProposta.RECUSADA));
            propostaRepository.saveAll(contrato.getPropostas());
        }

        return propostaRepository.save(p);
    }

    @Transactional
    public Proposta recusarProposta(Long propostaId) {
        Proposta p = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta não encontrada"));
        if (p.getStatus() != StatusProposta.PENDENTE) {
            throw new IllegalStateException("Somente propostas pendentes podem ser recusadas");
        }
        // somente o destinatário pode recusar
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        Usuario usuarioAutenticado = (Usuario) principal;
        if (p.getDestinatario() == null || !usuarioAutenticado.getId().equals(p.getDestinatario().getId())) {
            throw new AccessDeniedException("Somente o destinatário pode recusar a proposta");
        }

        p.setStatus(StatusProposta.RECUSADA);
        return propostaRepository.save(p);
    }

    @Transactional
    public Proposta criarContraproposta(Long propostaPaiId, CriarPropostaDTO dto) {
        dto.setPropostaPaiId(propostaPaiId);
        return criarProposta(dto);
    }
}
