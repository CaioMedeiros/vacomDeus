package modelo;

public class Creatina extends Suplemento {

    private String tipo;

    public Creatina(String nome, String marca, double preco, String tipo) {
        super(nome, marca, preco);
        this.tipo = tipo;
    }

    @Override public String getTipo() { return "Creatina"; }

    public String getTipoCreatina() { return tipo; }

    @Override
    public String toString() {
        return super.toString() + " | Tipo: " + tipo;
    }
}