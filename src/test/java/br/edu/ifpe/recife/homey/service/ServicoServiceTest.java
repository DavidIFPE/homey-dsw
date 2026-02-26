package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.AtualizarEnderecoDTO;
import br.edu.ifpe.recife.homey.dto.AtualizarServicoDTO;
import br.edu.ifpe.recife.homey.dto.CriarServicoDTO;
import br.edu.ifpe.recife.homey.dto.EnderecoDTO;
import br.edu.ifpe.recife.homey.dto.ServicoProximoDTO;
import br.edu.ifpe.recife.homey.dto.ViaCepResponse;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.entity.Coordenada;
import br.edu.ifpe.recife.homey.entity.Endereco;
import br.edu.ifpe.recife.homey.entity.Prestador;
import br.edu.ifpe.recife.homey.entity.Servico;
import br.edu.ifpe.recife.homey.entity.Usuario;
import br.edu.ifpe.recife.homey.factory.CategoriaFactory;
import br.edu.ifpe.recife.homey.factory.ClienteFactory;
import br.edu.ifpe.recife.homey.factory.EnderecoFactory;
import br.edu.ifpe.recife.homey.factory.PrestadorFactory;
import br.edu.ifpe.recife.homey.factory.ServicoFactory;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import br.edu.ifpe.recife.homey.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private NominatimService nominatimService;

    @Mock
    private ViaCepService viaCepService;

    @Mock
    private DistanceService distanceService;

    @InjectMocks
    private ServicoService servicoService;

    private Prestador prestador;

    @BeforeEach
    void setUp() {
        prestador = PrestadorFactory.criarPrestadorValido(1L);
    }

    @Test
    void listarTodos_deveRetornarTodosOsServicosDoRepositorio() {
        Servico servico1 = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        Servico servico2 = ServicoFactory.criarServicoValido(2L, prestador, EnderecoFactory.criarEnderecoValido(2L), List.of());
        given(servicoRepository.findAll()).willReturn(List.of(servico1, servico2));

        List<Servico> resultado = servicoService.listarTodos();

        assertThat(resultado).containsExactly(servico1, servico2);
        verify(servicoRepository).findAll();
    }

    @Test
    void listarPorPrestador_deveFiltrarServicosPeloIdDoPrestador() {
        Prestador outroPrestador = PrestadorFactory.criarPrestadorValido(2L);
        Servico servicoDoPrestador = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        Servico servicoDeOutroPrestador = ServicoFactory.criarServicoValido(2L, outroPrestador, EnderecoFactory.criarEnderecoValido(2L), List.of());

        given(servicoRepository.findAll()).willReturn(List.of(servicoDoPrestador, servicoDeOutroPrestador));

        List<Servico> resultado = servicoService.listarPorPrestador(prestador.getId());

        assertThat(resultado).containsExactly(servicoDoPrestador);
    }

    @Test
    void buscarPorCep_deveRetornarServicosOrdenadosPorDistancia() {
        String cep = "50000000";
        ViaCepResponse viaCepResponse = new ViaCepResponse(cep, "Rua", "Bairro", "Recife", "PE", false);
        given(viaCepService.buscarEnderecoPorCep(cep)).willReturn(viaCepResponse);
        given(viaCepService.montarEnderecoCompleto(viaCepResponse)).willReturn("endereco completo");

        Coordenada coordenadaUsuario = new Coordenada(-8.0476, -34.8770);
        given(nominatimService.obterCoordenadas("endereco completo")).willReturn(coordenadaUsuario);

        Endereco endereco1 = EnderecoFactory.criarEnderecoValido(1L);
        Endereco endereco2 = EnderecoFactory.criarEnderecoValido(2L);
        // garante coordenadas diferentes para testar ordenacao por distancia
        endereco2.setLatitude(-8.10);
        endereco2.setLongitude(-34.90);
        Servico servico1 = ServicoFactory.criarServicoValido(1L, prestador, endereco1, List.of());
        Servico servico2 = ServicoFactory.criarServicoValido(2L, prestador, endereco2, List.of());

        given(servicoRepository.findByDisponivelTrue()).willReturn(List.of(servico1, servico2));

        given(distanceService.calcularKm(coordenadaUsuario.latitude(), coordenadaUsuario.longitude(),
                endereco1.getLatitude(), endereco1.getLongitude()))
                .willReturn(10.123);
        given(distanceService.calcularKm(coordenadaUsuario.latitude(), coordenadaUsuario.longitude(),
                endereco2.getLatitude(), endereco2.getLongitude()))
                .willReturn(5.987);

        List<ServicoProximoDTO> resultado = servicoService.buscarPorCep(cep);

        assertThat(resultado).hasSize(2);
        // Deve vir ordenado pela menor distancia
        assertThat(resultado.get(0).id()).isEqualTo(servico2.getId());
        assertThat(resultado.get(0).distanciaKm()).isEqualTo(5.99); // arredondado a 2 casas
        assertThat(resultado.get(1).id()).isEqualTo(servico1.getId());
        assertThat(resultado.get(1).distanciaKm()).isEqualTo(10.12);
    }

    @Test
    void criar_deveLancarErroQuandoUsuarioNaoForPrestador() {
        Usuario cliente = ClienteFactory.criarClienteValido(10L);

        CriarServicoDTO dto = new CriarServicoDTO(
                "Titulo",
                "Descricao",
                BigDecimal.TEN,
                true,
                List.of("Limpeza"),
                new EnderecoDTO("Rua", "123", null, "Bairro", "Cidade", "PE", "50000000", -8.0, -34.0)
        );

        assertThatThrownBy(() -> servicoService.criar(dto, cliente))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Apenas prestadores podem criar serviços");
    }

    @Test
    void criar_deveCriarServicoQuandoPrestadorValido() {
        Categoria categoria = CategoriaFactory.criarCategoriaValida(1L, "Limpeza");
        given(categoriaRepository.findByNomeIgnoreCase("Limpeza")).willReturn(Optional.of(categoria));

        CriarServicoDTO dto = new CriarServicoDTO(
                "Titulo",
                "Descricao",
                BigDecimal.TEN,
                true,
                List.of("Limpeza"),
                new EnderecoDTO("Rua", "123", null, "Bairro", "Cidade", "PE", "50000000", -8.0, -34.0)
        );

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        given(servicoRepository.save(any(Servico.class))).willAnswer(invocation -> {
            Servico s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        Servico criado = servicoService.criar(dto, prestador);

        verify(servicoRepository).save(captor.capture());
        Servico salvo = captor.getValue();

        assertThat(criado.getId()).isEqualTo(1L);
        assertThat(salvo.getTitulo()).isEqualTo(dto.titulo());
        assertThat(salvo.getDescricao()).isEqualTo(dto.descricao());
        assertThat(salvo.getPrecoBase()).isEqualTo(dto.precoBase());
        assertThat(salvo.getDisponivel()).isTrue();
        assertThat(salvo.getPrestador()).isEqualTo(prestador);
        assertThat(salvo.getEndereco()).isNotNull();
        assertThat(salvo.getCategorias()).extracting(Categoria::getNome).containsExactly("Limpeza");
    }

    @Test
    void atualizar_deveLancarErroQuandoUsuarioNaoForDonoDoServico() {
        Servico servico = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        Prestador outroPrestador = PrestadorFactory.criarPrestadorValido(2L);

        given(servicoRepository.findById(1L)).willReturn(Optional.of(servico));

        AtualizarServicoDTO dto = new AtualizarServicoDTO("Novo titulo", null, null, null, null, null);

        assertThatThrownBy(() -> servicoService.atualizar(1L, dto, outroPrestador))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para atualizar este serviço");
    }

    @Test
    void atualizar_deveAtualizarCamposQuandoUsuarioEhDono() {
        Endereco endereco = EnderecoFactory.criarEnderecoValido(1L);
        Servico servico = ServicoFactory.criarServicoValido(1L, prestador, endereco, List.of());

        given(servicoRepository.findById(1L)).willReturn(Optional.of(servico));
        Categoria novaCategoria = CategoriaFactory.criarCategoriaValida(2L, "Jardinagem");
        given(categoriaRepository.findByNomeIgnoreCase("Jardinagem")).willReturn(Optional.of(novaCategoria));

        AtualizarEnderecoDTO atualizarEnderecoDTO = new AtualizarEnderecoDTO(
                null, null, null, "Novo Bairro", null, null, null, null, null
        );

        AtualizarServicoDTO dto = new AtualizarServicoDTO(
                "Novo titulo",
                "Nova descricao",
                BigDecimal.valueOf(200.0),
                false,
                List.of("Jardinagem"),
                atualizarEnderecoDTO
        );

        given(servicoRepository.save(any(Servico.class))).willAnswer(invocation -> invocation.getArgument(0));

        Servico atualizado = servicoService.atualizar(1L, dto, prestador);

        assertThat(atualizado.getTitulo()).isEqualTo("Novo titulo");
        assertThat(atualizado.getDescricao()).isEqualTo("Nova descricao");
        assertThat(atualizado.getPrecoBase()).isEqualTo(BigDecimal.valueOf(200.0));
        assertThat(atualizado.getDisponivel()).isFalse();
        assertThat(atualizado.getCategorias()).extracting(Categoria::getNome).containsExactly("Jardinagem");
        assertThat(atualizado.getEndereco().getBairro()).isEqualTo("Novo Bairro");
    }

    @Test
    void deletar_deveLancarErroQuandoUsuarioNaoForDonoDoServico() {
        Servico servico = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        Prestador outroPrestador = PrestadorFactory.criarPrestadorValido(2L);

        given(servicoRepository.findById(1L)).willReturn(Optional.of(servico));

        assertThatThrownBy(() -> servicoService.deletar(1L, outroPrestador))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para deletar este serviço");

        verify(servicoRepository, never()).delete(any(Servico.class));
    }

    @Test
    void deletar_deveRemoverServicoQuandoUsuarioEhDono() {
        Servico servico = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        given(servicoRepository.findById(1L)).willReturn(Optional.of(servico));

        servicoService.deletar(1L, prestador);

        verify(servicoRepository).delete(servico);
    }

    @Test
    void alterarDisponibilidade_deveLancarErroQuandoUsuarioNaoForDono() {
        Servico servico = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        Prestador outroPrestador = PrestadorFactory.criarPrestadorValido(2L);

        given(servicoRepository.findById(1L)).willReturn(Optional.of(servico));

        assertThatThrownBy(() -> servicoService.alterarDisponibilidade(1L, false, outroPrestador))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para alterar este serviço");
    }

    @Test
    void alterarDisponibilidade_deveAlterarQuandoUsuarioEhDono() {
        Servico servico = ServicoFactory.criarServicoValido(1L, prestador, EnderecoFactory.criarEnderecoValido(1L), List.of());
        given(servicoRepository.findById(1L)).willReturn(Optional.of(servico));
        given(servicoRepository.save(any(Servico.class))).willAnswer(invocation -> invocation.getArgument(0));

        Servico atualizado = servicoService.alterarDisponibilidade(1L, false, prestador);

        assertThat(atualizado.getDisponivel()).isFalse();
        verify(servicoRepository).save(servico);
    }
}
