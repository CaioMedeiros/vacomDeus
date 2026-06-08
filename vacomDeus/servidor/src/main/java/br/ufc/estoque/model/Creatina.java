package br.ufc.estoque.model;

public class Creatina extends Suplemento {

    private String tipoCreatina;

    public Creatina() {}

    public Creatina(String nome, String marca, double preco, String tipoCreatina) {
        super(nome, marca, preco);
        this.tipoCreatina = tipoCreatina;
    }

    @Override public String getTipo()              { return "Creatina"; }
    public String           getTipoCreatina()      { return tipoCreatina; }
    public void             setTipoCreatina(String t) { this.tipoCreatina = t; }
}
