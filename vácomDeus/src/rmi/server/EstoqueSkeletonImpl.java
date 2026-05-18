package rmi.server;

import estoque.Estoque;
import estoque.Lote;
import modelo.Suplemento;
import rmi.common.EstoqueService;

import java.util.List;

public class EstoqueSkeletonImpl implements EstoqueService {

    private final Estoque estoque;

    public EstoqueSkeletonImpl(Estoque estoque) {
        this.estoque = estoque;
    }

    @Override
    public String adicionarSuplemento(Suplemento s) {
        return estoque.adicionarItem(s);
    }

    @Override
    public String adicionarLote(String nomeSuplemento, Lote lote) {
        Suplemento s = estoque.buscarItem(nomeSuplemento);
        if (s == null) return "✘ Suplemento não encontrado: " + nomeSuplemento;
        s.adicionarLote(lote);
        return "✔ Lote " + lote.getCodigo() + " adicionado a " + nomeSuplemento;
    }

    @Override
    public Suplemento buscarSuplemento(String nome) {
        return estoque.buscarItem(nome);
    }

    @Override
    public List<Suplemento> listarEstoque() {
        return estoque.listarTodos();
    }

    @Override
    public List<Suplemento> listarVencidos() {
        return estoque.listarVencidos();
    }

    @Override
    public List<Suplemento> listarProximosAoVencer(int dias) {
        return estoque.listarProximosAoVencer(dias);
    }

    @Override
    public String removerSuplemento(String nome) {
        return estoque.removerItem(nome);
    }
}
