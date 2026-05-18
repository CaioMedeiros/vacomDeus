package modelo;

import estoque.Lote;
import interfaces.Validavel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public abstract class Suplemento implements Validavel, Serializable {

    private static final long serialVersionUID = 1L;

    private String     nome;
    private String     marca;
    private double     preco;
    private List<Lote> lotes;

    public Suplemento(String nome, String marca, double preco) {
        this.nome  = nome;
        this.marca = marca;
        this.preco = preco;
        this.lotes = new ArrayList<>();
    }

    @Override
    public LocalDate getDataVencimento() {
        return lotes.stream()
                .map(Lote::getDataVencimento)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    @Override
    public boolean estaValido() {
        LocalDate venc = getDataVencimento();
        return venc != null && !venc.isBefore(LocalDate.now());
    }

    @Override
    public int getDiasParaVencer() {
        LocalDate venc = getDataVencimento();
        if (venc == null) return -1;
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), venc);
    }

    public void adicionarLote(Lote lote) { lotes.add(lote); }
    public List<Lote> getLotes()          { return lotes; }

    public int getQuantidadeTotal() {
        return lotes.stream().mapToInt(Lote::getQuantidade).sum();
    }

    public abstract String getTipo();

    public String getNome()               { return nome; }
    public String getMarca()              { return marca; }
    public double getPreco()              { return preco; }
    public void   setPreco(double preco)  { this.preco = preco; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s | R$ %.2f | Qtd: %d | Válido: %s | Vence em: %d dias",
                getTipo(), nome, marca, preco, getQuantidadeTotal(),
                estaValido() ? "Sim" : "NÃO", getDiasParaVencer());
    }
}
