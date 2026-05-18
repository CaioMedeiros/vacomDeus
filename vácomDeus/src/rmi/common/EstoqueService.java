package rmi.common;

import modelo.Suplemento;
import estoque.Lote;
import java.util.List;

public interface EstoqueService {

    String adicionarSuplemento(Suplemento s);
    String adicionarLote(String nomeSuplemento, Lote lote);
    Suplemento buscarSuplemento(String nome);
    List<Suplemento> listarEstoque();
    List<Suplemento> listarVencidos();
    List<Suplemento> listarProximosAoVencer(int dias);
    String removerSuplemento(String nome);
}
