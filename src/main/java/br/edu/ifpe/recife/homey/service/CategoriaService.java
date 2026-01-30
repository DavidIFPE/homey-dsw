package br.edu.ifpe.recife.homey.service;

import br.edu.ifpe.recife.homey.dto.CriarCategoriaDTO;
import br.edu.ifpe.recife.homey.entity.Categoria;
import br.edu.ifpe.recife.homey.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
    }

    public Categoria criar(CriarCategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Long id, CriarCategoriaDTO dto) {
        Categoria categoria = buscarPorId(id);
        categoria.setNome(dto.nome());
        return categoriaRepository.save(categoria);
    }

    public void deletar(Long id) {
        Categoria categoria = buscarPorId(id);
        categoriaRepository.delete(categoria);
    }
}
