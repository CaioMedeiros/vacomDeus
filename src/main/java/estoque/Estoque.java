package estoque;

import modelo.Suplemento;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Estoque {

    private String nome;
    private List<Suplemento> itens;

    public Estoque(String nome) {
        this.nome  = nome;
        this.itens = new ArrayList<>();
    }


    public void adicionarItem(Suplemento s) {
        itens.add(s);
        System.out.println("✔ Adicionado: " + s.getNome());
    }

    public boolean removerItem(String nome) {
        boolean removido = itens.removeIf(s -> s.getNome().equalsIgnoreCase(nome));
        System.out.println(removido ? "✔ Removido: " + nome : "✘ Não encontrado: " + nome);
        return removido;
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

    public void exibirEstoque() {
        System.out.println("\n═══ " + nome + " ═══");
        if (itens.isEmpty()) {
            System.out.println("  (vazio)");
        } else {
            itens.forEach(s -> System.out.println("  • " + s));
        }
        System.out.println("Total de itens: " + itens.size());
    }

    public String getNome()        { return nome; }
    public List<Suplemento> getItens() { return itens; }
}