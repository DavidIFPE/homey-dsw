package br.edu.ifpe.recife.homey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Proposta;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    List<Proposta> findByContratoOrderByDataCriacaoAsc(Contrato contrato);
}
