package br.edu.ifpe.recife.homey.service;

import java.util.List;
import java.util.Date;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpe.recife.homey.dto.CriarPropostaDTO;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.entity.Proposta.StatusProposta;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.entity.Contrato.StatusContrato;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import br.edu.ifpe.recife.homey.repository.PropostaRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import br.edu.ifpe.recife.homey.repository.UsuarioRepository;

@Service
public class PropostaService {

    private final PropostaRepository propostaRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public PropostaService(PropostaRepository propostaRepository,
                          ContratoRepository contratoRepository,
                          UsuarioRepository usuarioRepository,
                          ServicoRepository servicoRepository) {
        this.propostaRepository = propostaRepository;
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository; 
        this.servicoRepository = servicoRepository; 
    }

    @Transactional
    public Proposta criarProposta(CriarPropostaDTO dto) {
        // obter usuário autenticado e usar como remetente
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        Servico servico = servicoRepository.findById(dto.servicoId())
            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        Usuario usuarioAutenticado = (Usuario) principal;

        Usuario remetente = usuarioRepository.findById(usuarioAutenticado.getId())
            .orElseThrow(() -> new IllegalArgumentException("Remetente não encontrado"));

        Usuario destinatario = servico.getPrestador();

        if (destinatario != null && destinatario.getId().equals(remetente.getId())) {
            throw new IllegalArgumentException("Você não pode enviar proposta para o próprio serviço.");
        }

        boolean existePendente = propostaRepository.existsByRemetenteAndServicoAndStatus(remetente.getId(), servico.getId(), StatusProposta.PENDENTE);

        if (existePendente) {
        throw new IllegalStateException("Já existe uma proposta pendente para este serviço.");
        }


        Contrato contrato = new Contrato();
        contrato.setStatus(StatusContrato.PENDENTE);
        contrato.setCliente((Cliente)remetente);
        contrato.setServico(servico);

        Proposta p = new Proposta();
        p.setRemetente(remetente);
        p.setDestinatario(destinatario);
        p.setValor(dto.valor());
        p.setPrazoResposta(dto.prazoResposta());
        p.setMensagem(dto.mensagem());
        p.setStatus(StatusProposta.PENDENTE);
        p.setData_inicio(dto.dataInicio());
        p.setData_fim(dto.dataFim());
        contrato.addProposta(p);

        contratoRepository.save(contrato);
        return p;
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
        if(p.getContrato().getPropostaAceita() != null) {
            throw new IllegalStateException("Só pode haver uma proposta aceita.");
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
        contrato.setPropostaAceita(p);
        contrato.setData_inicio(p.getData_inicio());
        contrato.setData_fim(p.getData_fim());
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
    public Proposta criarContraproposta(Long contratoId, CriarPropostaDTO dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario atual)) throw new AccessDeniedException("Usuário não autenticado");

        Contrato contrato = contratoRepository.findById(contratoId)
            .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (contrato.getStatus() != Contrato.StatusContrato.PENDENTE)
            throw new IllegalStateException("Contrato não está pendente");
        if (contrato.getPropostaAceita() != null)
            throw new IllegalStateException("Contrato já possui proposta aceita");

        // (opcional) valide servicoId do DTO, se vier
        if (dto.servicoId() != null && !dto.servicoId().equals(contrato.getServico().getId()))
            throw new IllegalArgumentException("Serviço do DTO não corresponde ao do contrato");

        // obter a última proposta endereçada ao usuário atual
        Proposta base = propostaRepository
            .findTopByContratoIdAndDestinatarioIdOrderByDataCriacaoDesc(contratoId, atual.getId())
            .orElseThrow(() -> new IllegalStateException("Não há proposta endereçada a você para responder"));

        // inverter papéis a partir da última proposta recebida
        Usuario remetente = base.getDestinatario();  // eu (autenticado)
        Usuario destinatario = base.getRemetente();  // quem me enviou

        Proposta nova = new Proposta();
        nova.setRemetente(remetente);
        nova.setDestinatario(destinatario);
        nova.setValor(dto.valor());
        nova.setPrazoResposta(dto.prazoResposta());
        nova.setMensagem(dto.mensagem());
        nova.setStatus(StatusProposta.PENDENTE);
        nova.setData_inicio(dto.dataInicio());
        nova.setData_fim(dto.dataFim());

        contrato.addProposta(nova);
        contratoRepository.save(contrato);
        return nova;
    }

}
