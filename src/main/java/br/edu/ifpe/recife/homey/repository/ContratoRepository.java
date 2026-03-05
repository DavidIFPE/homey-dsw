package br.edu.ifpe.recife.homey.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifpe.recife.homey.entity.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
}
