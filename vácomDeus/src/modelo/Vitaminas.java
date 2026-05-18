package modelo;

public class Vitaminas extends Suplemento {

    private static final long serialVersionUID = 1L;

    private String formulacao;

    public Vitaminas(String nome, String marca, double preco, String formulacao) {
        super(nome, marca, preco);
        this.formulacao = formulacao;
    }

    @Override public String getTipo()       { return "Vitaminas"; }
    public String           getFormulacao() { return formulacao; }

    @Override
    public String toString() {
        return super.toString() + " | Formulação: " + formulacao;
    }
}
