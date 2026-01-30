package br.edu.ifpe.recife.homey.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpe.recife.homey.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
	Optional<Categoria> findByNomeIgnoreCase(String nome);
}
