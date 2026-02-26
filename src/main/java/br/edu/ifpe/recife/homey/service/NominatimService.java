package br.edu.ifpe.recife.homey.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import br.edu.ifpe.recife.homey.dto.NominatimResponse;
import br.edu.ifpe.recife.homey.entity.Coordenada;

@Service
public class NominatimService {

    private final RestTemplate restTemplate = new RestTemplate();

    private NominatimResponse buscarPrimeiroResultado(String enderecoCompleto) {

        String url = UriComponentsBuilder
            .fromUriString("https://nominatim.openstreetmap.org/search")
            .queryParam("format", "json")
            .queryParam("q", enderecoCompleto)
            .queryParam("limit", 1)
            .queryParam("countrycodes", "br")
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Homey-MVP/1.0 (ana.leticia@email.com)");
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<NominatimResponse[]> response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NominatimResponse[].class
            );

        if (response.getBody() == null || response.getBody().length == 0) {
            throw new IllegalArgumentException("Endereço não encontrado no Nominatim");
        }

        return response.getBody()[0];
    }

    public Coordenada obterCoordenadas(String enderecoCompleto) {
        NominatimResponse nominatim = buscarPrimeiroResultado(enderecoCompleto);

        return new Coordenada(
            Double.parseDouble(nominatim.lat()),
            Double.parseDouble(nominatim.lon())
        );
    }

    public NominatimResponse obterDetalhes(String enderecoCompleto) {
        return buscarPrimeiroResultado(enderecoCompleto);
    }
}
