package br.edu.ifpe.recife.homey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifpe.recife.homey.entity.Contrato;
import br.edu.ifpe.recife.homey.entity.Proposta;
import br.edu.ifpe.recife.homey.entity.Contrato.StatusContrato;
import br.edu.ifpe.recife.homey.entity.Proposta.StatusProposta;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    List<Proposta> findByContratoOrderByDataCriacaoAsc(Contrato contrato);
    
    @Query("""
           SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END
           FROM Proposta p
           JOIN p.contrato c
           JOIN c.servico s
           WHERE p.remetente.id = :remetenteId
             AND s.id = :servicoId
             AND p.status = :status
           """)
    boolean existsByRemetenteAndServicoAndStatus(
        @Param("remetenteId") Long remetenteId,
        @Param("servicoId") Long servicoId,
        @Param("status") Proposta.StatusProposta status
    );

}
