package br.edu.ifpe.recife.homey.config;

import br.edu.ifpe.recife.homey.dto.CriarClienteDTO;
import br.edu.ifpe.recife.homey.dto.CriarPrestadorDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoDTO;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import br.edu.ifpe.recife.homey.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Configuration
@Profile("dev")
public class DevDataConfig {

    @Bean
    CommandLineRunner initDevData(UsuarioService usuarioService,
                                  ServicoRepository servicoRepository,
                                  CategoriaRepository categoriaRepository,
                                ContratoRepository contratoRepository) {
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

            // ---------------------------------------------
        // SEED: Contratos e Propostas (vários cenários)
        // ---------------------------------------------

        // Recupera o cliente e o prestador criados acima
        // Ajuste a forma de busca conforme seus métodos disponíveis
        Cliente clienteDev = (Cliente) usuarioService.buscarPorUsername("cliente.dev")
                .orElseThrow(() -> new IllegalStateException("Cliente Dev não encontrado"));
        Prestador prestadorDev = (Prestador) usuarioService.buscarPorUsername("prestador.dev")
                .orElseThrow(() -> new IllegalStateException("Prestador Dev não encontrado"));

        // Pega alguns serviços criados (os 3 primeiros, por exemplo)
        List<Servico> servicosSeed = servicoRepository.findAll();
        Servico servico1 = servicosSeed.get(0);
        Servico servico2 = servicosSeed.size() > 1 ? servicosSeed.get(1) : servico1;
        Servico servico3 = servicosSeed.size() > 2 ? servicosSeed.get(2) : servico1;

        Date agora = new Date();
        Date daqui3Dias = new Date(agora.getTime() + 3L * 24 * 3600 * 1000);
        Date daqui5Dias = new Date(agora.getTime() + 5L * 24 * 3600 * 1000);
        Date inicioProxSemana = new Date(agora.getTime() + 7L * 24 * 3600 * 1000);
        Date fimProxSemana = new Date(agora.getTime() + 10L * 24 * 3600 * 1000);

        // Utilitário para criar proposta rapidamente
        java.util.function.Function<BigDecimal, Proposta> propostaBaseCliente = (valor) -> {
        Proposta p = new Proposta();
        p.setRemetente(clienteDev);
        p.setDestinatario(prestadorDev);
        p.setValor(valor);
        p.setMensagem("Proposta inicial do cliente");
        p.setStatus(Proposta.StatusProposta.PENDENTE);
        p.setPrazoResposta(daqui3Dias);
        p.setData_inicio(null);
        p.setData_fim(null);
        return p;
        };

        java.util.function.Function<BigDecimal, Proposta> propostaBasePrestador = (valor) -> {
        Proposta p = new Proposta();
        p.setRemetente(prestadorDev);
        p.setDestinatario(clienteDev);
        p.setValor(valor);
        p.setMensagem("Contraproposta do prestador");
        p.setStatus(Proposta.StatusProposta.PENDENTE);
        p.setPrazoResposta(daqui5Dias);
        p.setData_inicio(null);
        p.setData_fim(null);
        return p;
        };

        // ========== CENÁRIO 1: Contrato PENDENTE com propostas pendentes (ida e volta) ==========
        Contrato contratoPendente = new Contrato();
        contratoPendente.setStatus(Contrato.StatusContrato.PENDENTE);
        contratoPendente.setCliente(clienteDev);
        contratoPendente.setServico(servico1);

        // Histórico: cliente propõe, prestador contrapropõe (ambas PENDENTES)
        Proposta c1_p1 = propostaBaseCliente.apply(new BigDecimal("150.00"));
        Proposta c1_p2 = propostaBasePrestador.apply(new BigDecimal("180.00"));
        contratoPendente.addProposta(c1_p1);
        contratoPendente.addProposta(c1_p2);

        contratoPendente = contratoRepository.save(contratoPendente); // cascade salva propostas

        // ========== CENÁRIO 2: Contrato ATIVO (proposta aceita; demais recusadas) ==========
        Contrato contratoAtivo = new Contrato();
        contratoAtivo.setStatus(Contrato.StatusContrato.PENDENTE);
        contratoAtivo.setCliente(clienteDev);
        contratoAtivo.setServico(servico2);

        // Histórico: cliente propõe, prestador contrapropõe, cliente mantém proposta inicial → prestador aceita a inicial
        Proposta c2_p1 = propostaBaseCliente.apply(new BigDecimal("200.00"));
        c2_p1.setMensagem("Posso fechar por 200");
        c2_p1.setData_inicio(inicioProxSemana);
        c2_p1.setData_fim(fimProxSemana);

        Proposta c2_p2 = propostaBasePrestador.apply(new BigDecimal("220.00"));
        c2_p2.setMensagem("Consigo por 220");

        contratoAtivo.addProposta(c2_p1);
        contratoAtivo.addProposta(c2_p2);

        // Prestador aceita a proposta do cliente (c2_p1)
        c2_p1.setStatus(Proposta.StatusProposta.ACEITA);
        c2_p2.setStatus(Proposta.StatusProposta.RECUSADA);

        contratoAtivo.setPropostaAceita(c2_p1); // já seta status ATIVO e valor_final
        contratoAtivo.setData_inicio(c2_p1.getData_inicio());
        contratoAtivo.setData_fim(c2_p1.getData_fim());

        contratoAtivo = contratoRepository.save(contratoAtivo);

        // ========== CENÁRIO 3: Contrato CONCLUÍDO ==========
        Contrato contratoConcluido = new Contrato();
        contratoConcluido.setStatus(Contrato.StatusContrato.PENDENTE);
        contratoConcluido.setCliente(clienteDev);
        contratoConcluido.setServico(servico3);

        Proposta c3_p1 = propostaBaseCliente.apply(new BigDecimal("180.00"));
        c3_p1.setMensagem("Proposta para concluir serviço");

        contratoConcluido.addProposta(c3_p1);

        // Prestador aceita
        c3_p1.setStatus(Proposta.StatusProposta.ACEITA);
        contratoConcluido.setPropostaAceita(c3_p1);
        contratoConcluido.setData_inicio(inicioProxSemana);
        contratoConcluido.setData_fim(fimProxSemana);

        // Marca como CONCLUÍDO (após execução do serviço)
        contratoConcluido.setStatus(Contrato.StatusContrato.CONCLUIDO);

        contratoConcluido = contratoRepository.save(contratoConcluido);

        // ========== CENÁRIO 4: Contrato CANCELADO (todas propostas recusadas) ==========
        Contrato contratoCancelado = new Contrato();
        contratoCancelado.setStatus(Contrato.StatusContrato.PENDENTE);
        contratoCancelado.setCliente(clienteDev);
        contratoCancelado.setServico(servico1);

        Proposta c4_p1 = propostaBaseCliente.apply(new BigDecimal("120.00"));
        Proposta c4_p2 = propostaBasePrestador.apply(new BigDecimal("200.00"));
        Proposta c4_p3 = propostaBaseCliente.apply(new BigDecimal("160.00"));
        c4_p1.setMensagem("Cliente oferta 120");
        c4_p2.setMensagem("Prestador pede 200");
        c4_p3.setMensagem("Cliente melhora para 160");

        contratoCancelado.addProposta(c4_p1);
        contratoCancelado.addProposta(c4_p2);
        contratoCancelado.addProposta(c4_p3);

        // Negociação falha: prestador recusa última; contrato sem aceite e cancelado
        c4_p1.setStatus(Proposta.StatusProposta.RECUSADA);
        c4_p2.setStatus(Proposta.StatusProposta.RECUSADA);
        c4_p3.setStatus(Proposta.StatusProposta.RECUSADA);
        contratoCancelado.setStatus(Contrato.StatusContrato.CANCELADO);

        contratoCancelado = contratoRepository.save(contratoCancelado);

        // ========== CENÁRIO 5: Contrato PENDENTE com longa thread (inclui contrapropostas) ==========
        Contrato contratoLongaThread = new Contrato();
        contratoLongaThread.setStatus(Contrato.StatusContrato.PENDENTE);
        contratoLongaThread.setCliente(clienteDev);
        contratoLongaThread.setServico(servico2);

        // Thread: C → P → C → P (todas pendentes)
        Proposta c5_p1 = propostaBaseCliente.apply(new BigDecimal("140.00")); c5_p1.setMensagem("1) Cliente 140");
        Proposta c5_p2 = propostaBasePrestador.apply(new BigDecimal("210.00")); c5_p2.setMensagem("2) Prestador 210");
        Proposta c5_p3 = propostaBaseCliente.apply(new BigDecimal("170.00")); c5_p3.setMensagem("3) Cliente 170");
        Proposta c5_p4 = propostaBasePrestador.apply(new BigDecimal("190.00")); c5_p4.setMensagem("4) Prestador 190");

        contratoLongaThread.addProposta(c5_p1);
        contratoLongaThread.addProposta(c5_p2);
        contratoLongaThread.addProposta(c5_p3);
        contratoLongaThread.addProposta(c5_p4);

        contratoLongaThread = contratoRepository.save(contratoLongaThread);
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
