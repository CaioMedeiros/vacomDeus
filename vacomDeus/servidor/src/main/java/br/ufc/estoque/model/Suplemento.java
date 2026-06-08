package br.ufc.estoque.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
    @JsonSubTypes.Type(value = WheyProtein.class,  name = "Whey Protein"),
    @JsonSubTypes.Type(value = Creatina.class,     name = "Creatina"),
    @JsonSubTypes.Type(value = Vitaminas.class,    name = "Vitaminas"),
    @JsonSubTypes.Type(value = PreTreino.class,    name = "Pré-Treino"),
})
public abstract class Suplemento {

    private String     nome;
    private String     marca;
    private double     preco;
    private List<Lote> lotes = new ArrayList<>();

    public Suplemento() {}

    public Suplemento(String nome, String marca, double preco) {
        this.nome  = nome;
        this.marca = marca;
        this.preco = preco;
    }

    public abstract String getTipo();

    public LocalDate getDataVencimento() {
        return lotes.stream()
                .map(Lote::getDataVencimento)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    public boolean estaValido() {
        LocalDate v = getDataVencimento();
        return v != null && !v.isBefore(LocalDate.now());
    }

    public int getDiasParaVencer() {
        LocalDate v = getDataVencimento();
        return v == null ? -1 : (int) ChronoUnit.DAYS.between(LocalDate.now(), v);
    }

    public int getQuantidadeTotal() {
        return lotes.stream().mapToInt(Lote::getQuantidade).sum();
    }

    public void adicionarLote(Lote lote) { lotes.add(lote); }

    public String     getNome()              { return nome; }
    public void       setNome(String n)      { this.nome = n; }
    public String     getMarca()             { return marca; }
    public void       setMarca(String m)     { this.marca = m; }
    public double     getPreco()             { return preco; }
    public void       setPreco(double p)     { this.preco = p; }
    public List<Lote> getLotes()             { return lotes; }
    public void       setLotes(List<Lote> l) { this.lotes = l; }
}
