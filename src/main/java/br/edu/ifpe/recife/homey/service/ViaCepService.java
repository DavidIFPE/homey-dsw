package br.edu.ifpe.recife.homey.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import br.edu.ifpe.recife.homey.dto.ViaCepResponse;

@Service
public class ViaCepService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ViaCepResponse buscarEnderecoPorCep(String cep) {
        String url = "http://viacep.com.br/ws/" + cep + "/json/";

        ViaCepResponse response =
            restTemplate.getForObject(url, ViaCepResponse.class);

        if (response == null || Boolean.TRUE.equals(response.erro())) {
            throw new IllegalArgumentException("CEP inválido ou não encontrado");
        }

        return response;
    }

    public String montarEnderecoCompleto(ViaCepResponse viaCep) {
        // return String.format(
        //     "%s %s %s %s",
        //     viaCep.cep(),
        //     viaCep.localidade(),
        //     viaCep.uf(),
        //     "Brasil"
        // );
        return String.format(
            "%s %s %s",
            viaCep.cep(),
            viaCep.localidade(),
            viaCep.uf()
        );
    }
}
