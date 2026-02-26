package br.edu.ifpe.recife.homey.controller;

import br.edu.ifpe.recife.homey.dto.GeocodingResponseDTO;
import br.edu.ifpe.recife.homey.dto.NominatimResponse;
import br.edu.ifpe.recife.homey.dto.ViaCepResponse;
import br.edu.ifpe.recife.homey.service.NominatimService;
import br.edu.ifpe.recife.homey.service.ViaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/enderecos")
@Tag(name = "Endereços", description = "Consulta de coordenadas (latitude/longitude) por CEP ou endereço")
public class EnderecoController {

    private final ViaCepService viaCepService;
    private final NominatimService nominatimService;

    public EnderecoController(ViaCepService viaCepService, NominatimService nominatimService) {
        this.viaCepService = viaCepService;
        this.nominatimService = nominatimService;
    }

    @GetMapping("/coordenadas/cep")
    @Operation(
            summary = "Obter coordenadas por CEP",
            description = "Principal método de pesquisa. Usa ViaCEP para resolver o CEP e Nominatim para retornar latitude/longitude e mais detalhes."
    )
    public ResponseEntity<?> obterCoordenadasPorCep(
            @Parameter(description = "CEP no formato apenas números, ex: 50000000")
            @RequestParam String cep
    ) {
        try {
            ViaCepResponse viaCep = viaCepService.buscarEnderecoPorCep(cep);
            String enderecoCompleto = viaCepService.montarEnderecoCompleto(viaCep);
            NominatimResponse nominatim = nominatimService.obterDetalhes(enderecoCompleto);

            GeocodingResponseDTO resposta = GeocodingResponseDTO.fromViaCepAndNominatim(
                    enderecoCompleto,
                    viaCep,
                    nominatim
            );

            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/coordenadas")
    @Operation(
            summary = "Obter coordenadas por endereço",
            description = "Permite pesquisar latitude/longitude a partir de cidade, rua, bairro, número, CEP, etc., retornando também detalhes do Nominatim."
    )
    public ResponseEntity<?> obterCoordenadasPorEndereco(
            @Parameter(description = "Logradouro (rua, avenida, etc.)")
            @RequestParam(required = false) String rua,

            @Parameter(description = "Número do imóvel")
            @RequestParam(required = false) String numero,

            @Parameter(description = "Bairro")
            @RequestParam(required = false) String bairro,

            @Parameter(description = "Cidade")
            @RequestParam(required = false) String cidade,

            @Parameter(description = "UF (estado), ex: PE")
            @RequestParam(required = false) String estado,

            @Parameter(description = "CEP, opcional na pesquisa por endereço")
            @RequestParam(required = false) String cep
    ) {
        List<String> partes = new ArrayList<>();

        if (rua != null && !rua.isBlank()) partes.add(rua.trim());
        if (numero != null && !numero.isBlank()) partes.add(numero.trim());
        if (bairro != null && !bairro.isBlank()) partes.add(bairro.trim());
        if (cidade != null && !cidade.isBlank()) partes.add(cidade.trim());
        if (estado != null && !estado.isBlank()) partes.add(estado.trim());
        if (cep != null && !cep.isBlank()) partes.add(cep.trim());

        if (partes.isEmpty()) {
            return ResponseEntity.badRequest().body("Pelo menos um parâmetro de endereço deve ser informado.");
        }

        String enderecoCompleto = String.join(" ", partes) + ", Brasil";

        try {
            NominatimResponse nominatim = nominatimService.obterDetalhes(enderecoCompleto);

            GeocodingResponseDTO resposta = GeocodingResponseDTO.fromNominatim(
                    enderecoCompleto,
                    nominatim
            );

            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
