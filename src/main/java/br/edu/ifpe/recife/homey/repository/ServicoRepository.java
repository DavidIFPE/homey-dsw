package br.edu.ifpe.recife.homey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifpe.recife.homey.entity.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByDisponivelTrue();

    List<Servico> findByCategorias_NomeIgnoreCase(String nome);
}
