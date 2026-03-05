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
    public Proposta criarContraproposta(Long propostaBaseId, CriarPropostaDTO dto) {
        Proposta base = propostaRepository.findById(propostaBaseId)
            .orElseThrow(() -> new IllegalArgumentException("Proposta base não encontrada"));

        Contrato contrato = base.getContrato();
        if (contrato.getStatus() != Contrato.StatusContrato.PENDENTE) {
            throw new IllegalStateException("Contrato não está pendente");
        }
        if (contrato.getPropostaAceita() != null) {
            throw new IllegalStateException("Contrato já possui proposta aceita");
        }

        // valida serviço (se quiser usar o servicoId do DTO)
        if (dto.servicoId() != null && !dto.servicoId().equals(contrato.getServico().getId())) {
            throw new IllegalArgumentException("Serviço do DTO não corresponde ao do contrato");
        }

        // define remetente/destinatário invertendo os papéis da base
        Usuario remetente = base.getDestinatario();
        Usuario destinatario = base.getRemetente();
        if (remetente == null || destinatario == null || remetente.getId().equals(destinatario.getId())) {
            throw new IllegalStateException("Remetente/Destinatário inválidos para contraproposta");
        }

        // validações de negócio essenciais
        if (dto.valor() == null || dto.valor().signum() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        if (dto.prazoResposta() != null && dto.prazoResposta().before(new Date())) {
            throw new IllegalArgumentException("Prazo de resposta deve ser no futuro");
        }
        if (dto.dataInicio() != null && dto.dataFim() != null && dto.dataInicio().after(dto.dataFim())) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }

        // (opcional) política para pendentes:
        // - bloquear se já existir PENDENTE do mesmo remetente neste contrato
        // boolean pendenteDoMesmoLado = propostaRepository
        //     .existsByContratoIdAndRemetenteIdAndStatus(contrato.getId(), remetente.getId(), StatusProposta.PENDENTE);
        // if (pendenteDoMesmoLado) throw new IllegalStateException("Já existe contraproposta pendente deste remetente");

        // criar proposta
        Proposta nova = new Proposta();
        nova.setRemetente(remetente);
        nova.setDestinatario(destinatario);
        nova.setValor(dto.valor());
        nova.setPrazoResposta(dto.prazoResposta());
        nova.setMensagem(dto.mensagem());
        nova.setStatus(StatusProposta.PENDENTE);
        nova.setData_inicio(dto.dataInicio());
        nova.setData_fim(dto.dataFim());

        // vincular ao mesmo contrato
        contrato.addProposta(nova);

        // persistir pelo agregado (ou diretamente, se preferir)
        contratoRepository.save(contrato);
        return nova;
    }
}
