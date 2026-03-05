package br.edu.ifpe.recife.homey.service;

import org.springframework.security.core.Authentication;

import br.edu.ifpe.recife.homey.dto.AtualizarEnderecoDTO;
import br.edu.ifpe.recife.homey.dto.AtualizarServicoDTO;
import br.edu.ifpe.recife.homey.dto.CriarPropostaDTO;
import br.edu.ifpe.recife.homey.dto.CriarServicoDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoDTO;
import br.edu.ifpe.recife.homey.dto.ServicoProximoDTO;
import br.edu.ifpe.recife.homey.dto.ViaCepResponse;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Cliente;
import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Contrato.StatusContrato;
import br.edu.ifpe.recife.homey.entity.Coordenada;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.entity.Proposta.StatusProposta;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.factory.CategoriaFactory;
import br.edu.ifpe.recife.homey.factory.ClienteFactory;
import br.edu.ifpe.recife.homey.factory.ContratoFactory;
import br.edu.ifpe.recife.homey.factory.EnderecoFactory;
import br.edu.ifpe.recife.homey.factory.PrestadorFactory;
import br.edu.ifpe.recife.homey.factory.PropostaFactory;
import br.edu.ifpe.recife.homey.factory.ServicoFactory;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import br.edu.ifpe.recife.homey.repository.ContratoRepository;
import br.edu.ifpe.recife.homey.repository.PropostaRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import br.edu.ifpe.recife.homey.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    @Mock private PropostaRepository propostaRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PropostaService propostaService;

    private Cliente cliente;        // remetente na criação
    private Prestador prestador;    // destinatário
    private Servico servico;

    @BeforeEach
    void setUp() {
        cliente = ClienteFactory.criarClienteValido(10L);
        prestador = PrestadorFactory.criarPrestadorValido(20L);
        servico = ServicoFactory.criarServicoValido(100L, prestador);

        // Helper: autentica como 'cliente' (para criar proposta) e como 'prestador' (aceitar/recusar)
        // Em cada teste, chamamos setAuthenticatedUser() com o usuário adequado.
        limparSecurityContext();
    }

    @AfterEach
    void tearDown() {
        limparSecurityContext();
    }

    // ---------- CRIAR PROPOSTA (happy path) ----------
    @Test
    void criarProposta_deveCriarPropostaPendenteEContratoPendente_noMesmoServico() {
        // Arrange
        setAuthenticatedUser(cliente); // remetente é o cliente
        given(servicoRepository.findById(100L)).willReturn(Optional.of(servico));
        given(usuarioRepository.findById(cliente.getId())).willReturn(Optional.of(cliente));

        // Cascade pelo contrato: ao salvar contrato, retorna contrato com ID
        given(contratoRepository.save(any(Contrato.class))).willAnswer(inv -> {
            Contrato c = inv.getArgument(0);
            c.setId(500L);
            // simula cascade gerando ID para a proposta adicionada
            if (c.getPropostas() != null) {
                long idx = 1;
                for (Proposta pp : c.getPropostas()) {
                    pp.setId(700L + idx++);
                }
            }
            return c;
        });

        CriarPropostaDTO dto = new CriarPropostaDTO(
                servico.getId(),
                new BigDecimal("150.00"),
                futurePlusHours(48), // prazoResposta
                null,
                null,
                "Mensagem inicial"
        );

        // Act
        Proposta criada = propostaService.criarProposta(dto);

        // Assert
        assertThat(criada.getId()).isNotNull();
        assertThat(criada.getStatus()).isEqualTo(StatusProposta.PENDENTE);
        assertThat(criada.getRemetente().getId()).isEqualTo(cliente.getId());
        assertThat(criada.getDestinatario().getId()).isEqualTo(prestador.getId());
        assertThat(criada.getValor()).isEqualTo(new BigDecimal("150.00"));
        assertThat(criada.getContrato()).isNotNull();
        assertThat(criada.getContrato().getId()).isEqualTo(500L);
        assertThat(criada.getContrato().getServico().getId()).isEqualTo(servico.getId());
        assertThat(criada.getContrato().getStatus()).isEqualTo(StatusContrato.PENDENTE);
        verify(contratoRepository).save(any(Contrato.class));
    }

    // ---------- ACEITAR PROPOSTA (happy path) ----------
    @Test
    void aceitarProposta_deveMarcarAceitaAtivarContratoERecusarDemaisPendentes() {
        // Arrange
        Contrato contrato = ContratoFactory.criarContratoPendente(300L, servico, cliente);
        Proposta pendente1 = PropostaFactory.criarPendente(1000L, cliente, prestador, contrato, new BigDecimal("200.00"));
        Proposta pendente2 = PropostaFactory.criarPendente(1001L, cliente, prestador, contrato, new BigDecimal("220.00"));
        contrato.setPropostas(List.of(pendente1, pendente2));

        // aceitar a pendente1 pelo destinatário (prestador)
        setAuthenticatedUser(prestador);

        given(propostaRepository.findById(1000L)).willReturn(Optional.of(pendente1));
        given(contratoRepository.save(any(Contrato.class))).willAnswer(inv -> inv.getArgument(0));
        given(propostaRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));
        given(propostaRepository.save(any(Proposta.class))).willAnswer(inv -> inv.getArgument(0));

        // Act
        Proposta aceita = propostaService.aceitarProposta(1000L);

        // Assert
        assertThat(aceita.getStatus()).isEqualTo(StatusProposta.ACEITA);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
        assertThat(contrato.getPropostaAceita()).isEqualTo(aceita);
        assertThat(contrato.getValor_final()).isEqualTo(new BigDecimal("200.00"));

        Proposta outra = contrato.getPropostas().stream()
                .filter(p -> !p.getId().equals(aceita.getId()))
                .findFirst().orElseThrow();
        assertThat(outra.getStatus()).isEqualTo(StatusProposta.RECUSADA);

        verify(contratoRepository).save(contrato);
        verify(propostaRepository).saveAll(contrato.getPropostas());
        verify(propostaRepository).save(aceita);
    }

    // ---------- RECUSAR PROPOSTA (happy path) ----------
    @Test
    void recusarProposta_deveMarcarRecusadaSemAlterarContrato() {
        // Arrange
        Contrato contrato = ContratoFactory.criarContratoPendente(301L, servico, cliente);
        Proposta pendente = PropostaFactory.criarPendente(1100L, cliente, prestador, contrato, new BigDecimal("180.00"));
        contrato.setPropostas(List.of(pendente));

        // recusa feita pelo destinatário (prestador)
        setAuthenticatedUser(prestador);

        given(propostaRepository.findById(1100L)).willReturn(Optional.of(pendente));
        given(propostaRepository.save(any(Proposta.class))).willAnswer(inv -> inv.getArgument(0));

        // Act
        Proposta recusada = propostaService.recusarProposta(1100L);

        // Assert
        assertThat(recusada.getStatus()).isEqualTo(StatusProposta.RECUSADA);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.PENDENTE); // contrato intocado
        verify(propostaRepository).save(pendente);
    }

    // ---------- CRIAR CONTRAPROPOSTA (happy path) ----------
    @Test
    void criarContraproposta_deveInverterRemetenteDestinatarioEAderirAoMesmoContrato() {
        // Arrange
        Contrato contrato = ContratoFactory.criarContratoPendente(302L, servico, cliente);
        Proposta base = PropostaFactory.criarPendente(1200L, cliente, prestador, contrato, new BigDecimal("250.00"));
        contrato.addProposta(base);

        // contraproposta é enviada por QUEM recebeu a base (prestador)
        setAuthenticatedUser(prestador); // se seu método usa SecurityContext; se não, ignore

        // Repositório da base
        given(propostaRepository.findById(1200L)).willReturn(Optional.of(base));
        // Salvar contrato para simular cascade
        given(contratoRepository.save(any(Contrato.class))).willAnswer(inv -> {
            Contrato c = inv.getArgument(0);
            // simula gerar ID para nova proposta
            if (c.getPropostas() != null) {
                for (Proposta pp : c.getPropostas()) {
                    if (pp.getId() == null) pp.setId(1300L);
                }
            }
            return c;
        });

        CriarPropostaDTO dto = new CriarPropostaDTO(
                servico.getId(),                 // mesmo serviço
                new BigDecimal("260.00"),        // novo valor
                futurePlusHours(24),             // novo prazo
                null,
                null,
                "Posso por 260"
        );

        // Act
        Proposta contraproposta = propostaService.criarContraproposta(1200L, dto);

        // Assert
        assertThat(contraproposta.getId()).isNotNull();
        assertThat(contraproposta.getStatus()).isEqualTo(StatusProposta.PENDENTE);
        assertThat(contraproposta.getContrato().getId()).isEqualTo(302L);
        // invertido: remetente = destinatário da base; destinatário = remetente da base
        assertThat(contraproposta.getRemetente().getId()).isEqualTo(prestador.getId());
        assertThat(contraproposta.getDestinatario().getId()).isEqualTo(cliente.getId());
        assertThat(contraproposta.getValor()).isEqualTo(new BigDecimal("260.00"));

        verify(contratoRepository).save(contrato);
    }

    // ----------------- Helpers -----------------

    private Date futurePlusHours(int hours) {
        return new Date(System.currentTimeMillis() + hours * 3600_000L);
    }

   
    private void setAuthenticatedUser(Usuario usuario) {
        var auth = new TestingAuthenticationToken(usuario, "N/A", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    private void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}