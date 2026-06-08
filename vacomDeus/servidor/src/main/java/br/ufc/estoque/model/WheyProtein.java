package br.ufc.estoque.model;

public class WheyProtein extends Suplemento {

    private double proteinas;
    private String sabor;

    public WheyProtein() {}

    public WheyProtein(String nome, String marca, double preco, double proteinas, String sabor) {
        super(nome, marca, preco);
        this.proteinas = proteinas;
        this.sabor     = sabor;
    }

    @Override public String getTipo()        { return "Whey Protein"; }
    public double           getProteinas()   { return proteinas; }
    public void             setProteinas(double p) { this.proteinas = p; }
    public String           getSabor()       { return sabor; }
    public void             setSabor(String s)     { this.sabor = s; }
}
