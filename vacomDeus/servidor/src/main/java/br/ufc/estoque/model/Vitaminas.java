package br.ufc.estoque.model;

public class Vitaminas extends Suplemento {

    private String formulacao;

    public Vitaminas() {}

    public Vitaminas(String nome, String marca, double preco, String formulacao) {
        super(nome, marca, preco);
        this.formulacao = formulacao;
    }

    @Override public String getTipo()           { return "Vitaminas"; }
    public String           getFormulacao()     { return formulacao; }
    public void             setFormulacao(String f) { this.formulacao = f; }
}
