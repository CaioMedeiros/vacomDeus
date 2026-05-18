package estoque;

import modelo.Suplemento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Estoque implements Serializable {

    private static final long serialVersionUID = 1L;

    private String           nome;
    private List<Suplemento> itens;

    public Estoque(String nome) {
        this.nome  = nome;
        this.itens = new ArrayList<>();
    }

    public String adicionarItem(Suplemento s) {
        itens.add(s);
        return "✔ Adicionado: " + s.getNome();
    }

    public String removerItem(String nome) {
        boolean removido = itens.removeIf(s -> s.getNome().equalsIgnoreCase(nome));
        return removido ? "✔ Removido: " + nome : "✘ Não encontrado: " + nome;
    }

    public Suplemento buscarItem(String nome) {
        return itens.stream()
                .filter(s -> s.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    public List<Suplemento> listarVencidos() {
        return itens.stream()
                .filter(s -> !s.estaValido())
                .collect(Collectors.toList());
    }

    public List<Suplemento> listarProximosAoVencer(int dias) {
        return itens.stream()
                .filter(s -> s.estaValido() && s.getDiasParaVencer() <= dias)
                .collect(Collectors.toList());
    }

    public List<Suplemento> listarTodos() {
        return new ArrayList<>(itens);
    }

    public String getNome()               { return nome; }
    public List<Suplemento> getItens()    { return itens; }
}
