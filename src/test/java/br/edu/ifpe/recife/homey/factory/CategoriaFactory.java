package br.edu.ifpe.recife.homey.factory;

import br.edu.ifpe.recife.homey.entity.Categoria;

public final class CategoriaFactory {

    private CategoriaFactory() {
    }

    public static Categoria criarCategoriaValida(Long id, String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        return categoria;
    }
}
