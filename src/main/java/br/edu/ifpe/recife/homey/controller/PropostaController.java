package br.edu.ifpe.recife.homey.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import java.net.URI;
import org.springframework.web.bind.annotation.*;

import br.edu.ifpe.recife.homey.dto.CriarPropostaDTO;
import br.edu.ifpe.recife.homey.dto.PropostaResponseDTO;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import br.edu.ifpe.recife.homey.service.PropostaService;

@RestController
@RequestMapping("/propostas")
public class PropostaController {

    private final PropostaService propostaService;
    private final ContratoRepository contratoRepository;

    public PropostaController(PropostaService propostaService, ContratoRepository contratoRepository) {
        this.propostaService = propostaService;
        this.contratoRepository = contratoRepository;
    }

    @PostMapping
    public ResponseEntity<PropostaResponseDTO> criar(@RequestBody CriarPropostaDTO dto) {
        Proposta p = propostaService.criarProposta(dto);
        PropostaResponseDTO body = toDto(p);
        URI location = URI.create("/propostas/" + p.getId());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<PropostaResponseDTO>> historico(@PathVariable Long contratoId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
        List<Proposta> historico = propostaService.listarHistorico(contrato);
        List<PropostaResponseDTO> dtos = historico.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/aceitar")
    public ResponseEntity<PropostaResponseDTO> aceitar(@PathVariable Long id) {
        Proposta p = propostaService.aceitarProposta(id);
        return ResponseEntity.ok(toDto(p));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<PropostaResponseDTO> recusar(@PathVariable Long id) {
        Proposta p = propostaService.recusarProposta(id);
        return ResponseEntity.ok(toDto(p));
    }

    @PostMapping("/{id}/contrapropor")
    public ResponseEntity<PropostaResponseDTO> contrapropor(@PathVariable Long id, @RequestBody CriarPropostaDTO dto) {
        Proposta p = propostaService.criarContraproposta(id, dto);
        return ResponseEntity.ok(toDto(p));
    }

    private PropostaResponseDTO toDto(Proposta p) {
        PropostaResponseDTO dto = new PropostaResponseDTO();
        dto.setId(p.getId());
        dto.setContratoId(p.getContrato() != null ? p.getContrato().getId() : null);
        dto.setRemetenteId(p.getRemetente() != null ? p.getRemetente().getId() : null);
        dto.setDestinatarioId(p.getDestinatario() != null ? p.getDestinatario().getId() : null);
        dto.setValor(p.getValor());
        dto.setMensagem(p.getMensagem());
        dto.setPrazoResposta(p.getPrazoResposta());
        dto.setStatus(p.getStatus());
        dto.setPropostaPaiId(p.getPropostaPai() != null ? p.getPropostaPai().getId() : null);
        dto.setDataCriacao(p.getDataCriacao());
        return dto;
    }
}
