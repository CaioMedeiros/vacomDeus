package estoque;

import java.io.Serializable;
import java.time.LocalDate;

public class Lote implements Serializable {

    private static final long serialVersionUID = 1L;

    private String    codigo;
    private int       quantidade;
    private LocalDate dataFabricacao;
    private LocalDate dataVencimento;

    public Lote(String codigo, int quantidade,
                LocalDate dataFabricacao, LocalDate dataVencimento) {
        this.codigo         = codigo;
        this.quantidade     = quantidade;
        this.dataFabricacao = dataFabricacao;
        this.dataVencimento = dataVencimento;
    }

    public String    getCodigo()          { return codigo; }
    public int       getQuantidade()      { return quantidade; }
    public void      setQuantidade(int q) { this.quantidade = q; }
    public LocalDate getDataFabricacao()  { return dataFabricacao; }
    public LocalDate getDataVencimento()  { return dataVencimento; }

    @Override
    public String toString() {
        return String.format("Lote[%s | Qtd: %d | Fab: %s | Venc: %s]",
                codigo, quantidade, dataFabricacao, dataVencimento);
    }
}
