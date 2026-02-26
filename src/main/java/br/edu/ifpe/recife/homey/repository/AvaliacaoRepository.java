package br.edu.ifpe.recife.homey.repository;

import br.edu.ifpe.recife.homey.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Optional<Avaliacao> findByContratoId(Long contratoId);
    
    List<Avaliacao> findByClienteId(Long clienteId);
    
    List<Avaliacao> findByContrato_Servico_PrestadorId(Long prestadorId);
}
