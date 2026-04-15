package modelo;

public class WheyProtein extends Suplemento {

    private double proteinas;
    private String sabor;

    public WheyProtein(String nome, String marca, double preco,
                       double proteinas, String sabor) {
        super(nome, marca, preco);
        this.proteinas = proteinas;
        this.sabor     = sabor;
    }

    @Override public String getTipo() { return "Whey Protein"; }

    public double getProteinas() { return proteinas; }
    public String getSabor()     { return sabor; }

    @Override
    public String toString() {
        return super.toString() +
                String.format(" | Proteínas: %.1fg | Sabor: %s", proteinas, sabor);
    }
}