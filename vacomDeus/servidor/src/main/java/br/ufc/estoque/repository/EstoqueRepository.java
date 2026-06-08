package br.ufc.estoque.repository;

import br.ufc.estoque.model.Suplemento;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto distribuído 1 — repositório central em memória.
 * Gerencia a lista de suplementos do estoque.
 */
@Repository
public class EstoqueRepository {

    private final List<Suplemento> estoque = new ArrayList<>();

    public List<Suplemento> findAll() {
        return List.copyOf(estoque);
    }

    public Optional<Suplemento> findByNome(String nome) {
        return estoque.stream()
                .filter(s -> s.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    public void save(Suplemento s) {
        estoque.add(s);
    }

    public boolean deleteByNome(String nome) {
        return estoque.removeIf(s -> s.getNome().equalsIgnoreCase(nome));
    }

    public boolean existsByNome(String nome) {
        return estoque.stream().anyMatch(s -> s.getNome().equalsIgnoreCase(nome));
    }
}
