package br.edu.ifpe.recife.homey.config;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoDTO;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import br.edu.ifpe.recife.homey.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("dev")
public class DevDataConfig {

    @Bean
    CommandLineRunner initDevData(UsuarioService usuarioService,
                                  ServicoRepository servicoRepository,
                                  CategoriaRepository categoriaRepository) {
        return args -> {
            if (!servicoRepository.findAll().isEmpty()) {
                return; // já tem dados, não semear novamente
            }

            // Categorias base
            Categoria limpeza = new Categoria();
            limpeza.setNome("Limpeza");
            limpeza = categoriaRepository.save(limpeza);

            Categoria eletrica = new Categoria();
            eletrica.setNome("Elétrica");
            eletrica = categoriaRepository.save(eletrica);

            Categoria encanamento = new Categoria();
            encanamento.setNome("Encanamento");
            encanamento = categoriaRepository.save(encanamento);

            Categoria pintura = new Categoria();
            pintura.setNome("Pintura");
            pintura = categoriaRepository.save(pintura);

            // Cliente dev
            CriarClienteDTO clienteDTO = new CriarClienteDTO(
                    "Cliente Dev",
                    "cliente.dev@homey.com",
                    "cliente.dev",
                    "123456",
                    LocalDate.of(1995, 1, 10),
                    "81999990000",
                    "12345678901",
                    new EnderecoDTO(
                            "Rua do Cliente Dev",
                            "100",
                            null,
                            "Boa Vista",
                            "Recife",
                            "PE",
                            "50000000",
                            -8.061750,
                            -34.871140
                    )
            );

            usuarioService.criaCliente(clienteDTO);

            // Prestador dev
            CriarPrestadorDTO prestadorDTO = new CriarPrestadorDTO(
                    "Prestador Dev",
                    "prestador.dev@homey.com",
                    "prestador.dev",
                    "123456",
                    LocalDate.of(1990, 5, 20),
                    "81988880000",
                    "12345678000199",
                    new EnderecoDTO(
                            "Rua do Prestador Dev",
                            "200",
                            null,
                            "Casa Forte",
                            "Recife",
                            "PE",
                            "52060000",
                            -8.032220,
                            -34.922590
                    )
            );

            Prestador prestador = usuarioService.criaPrestador(prestadorDTO);

            // Serviços em Recife e região metropolitana
            criarServico(
                    servicoRepository,
                    prestador,
                    "Limpeza residencial em Boa Viagem",
                    "Limpeza completa de apartamentos e casas em Boa Viagem.",
                    new BigDecimal("150.00"),
                    true,
                    Arrays.asList(limpeza),
                    new EnderecoDTO(
                            "Avenida Boa Viagem",
                            "1500",
                            null,
                            "Boa Viagem",
                            "Recife",
                            "PE",
                            "51020000",
                            -8.126630,
                            -34.902780
                    )
            );

            criarServico(
                    servicoRepository,
                    prestador,
                    "Serviços elétricos em Casa Amarela",
                    "Manutenção e instalação elétrica residencial.",
                    new BigDecimal("200.00"),
                    true,
                    Arrays.asList(eletrica),
                    new EnderecoDTO(
                            "Rua da Harmonia",
                            "300",
                            null,
                            "Casa Amarela",
                            "Recife",
                            "PE",
                            "52070000",
                            -8.027560,
                            -34.919120
                    )
            );

            criarServico(
                    servicoRepository,
                    prestador,
                    "Encanador em Olinda",
                    "Conserto de vazamentos e troca de encanamentos.",
                    new BigDecimal("180.00"),
                    true,
                    Arrays.asList(encanamento),
                    new EnderecoDTO(
                            "Avenida Getúlio Vargas",
                            "400",
                            null,
                            "Bairro Novo",
                            "Olinda",
                            "PE",
                            "53030000",
                            -8.015430,
                            -34.840780
                    )
            );

            criarServico(
                    servicoRepository,
                    prestador,
                    "Pintura em Jaboatão dos Guararapes",
                    "Pintura interna e externa de casas e apartamentos.",
                    new BigDecimal("250.00"),
                    true,
                    Arrays.asList(pintura),
                    new EnderecoDTO(
                            "Rua Barão de Lucena",
                            "250",
                            null,
                            "Piedade",
                            "Jaboatão dos Guararapes",
                            "PE",
                            "54410000",
                            -8.175560,
                            -34.923820
                    )
            );

            criarServico(
                    servicoRepository,
                    prestador,
                    "Limpeza pós-obra em Paulista",
                    "Limpeza pesada após reforma ou construção.",
                    new BigDecimal("300.00"),
                    true,
                    Arrays.asList(limpeza),
                    new EnderecoDTO(
                            "Avenida Brasil",
                            "500",
                            null,
                            "Centro",
                            "Paulista",
                            "PE",
                            "53401000",
                            -7.940340,
                            -34.876230
                    )
            );
        };
    }

        private void criarServico(ServicoRepository servicoRepository,
                                                          Prestador prestador,
                                                          String titulo,
                                                          String descricao,
                                                          BigDecimal preco,
                                                          boolean disponivel,
                                                          List<Categoria> categorias,
                                                          EnderecoDTO enderecoDTO) {

        Servico servico = new Servico();
        servico.setTitulo(titulo);
        servico.setDescricao(descricao);
        servico.setPrecoBase(preco);
        servico.setDisponivel(disponivel);
        servico.setPrestador(prestador);

        if (categorias != null) {
            servico.setCategorias(categorias);
        }

        Endereco endereco = new Endereco();
        endereco.setLogradouro(enderecoDTO.logradouro());
        endereco.setNumero(enderecoDTO.numero());
        endereco.setComplemento(enderecoDTO.complemento());
        endereco.setBairro(enderecoDTO.bairro());
        endereco.setCidade(enderecoDTO.cidade());
        endereco.setEstado(enderecoDTO.estado());
        endereco.setCep(enderecoDTO.cep());
        endereco.setLatitude(enderecoDTO.latitude());
        endereco.setLongitude(enderecoDTO.longitude());

        servico.setEndereco(endereco);

        servicoRepository.save(servico);
    }
}
