package modelo;

public class PreTreino extends Suplemento {

    private int cafeina;
    private boolean temBeta;

    public PreTreino(String nome, String marca, double preco,
                     int cafeina, boolean temBeta) {
        super(nome, marca, preco);
        this.cafeina = cafeina;
        this.temBeta = temBeta;
    }

    @Override public String getTipo() { return "Pré-Treino"; }

    public int getCafeina()   { return cafeina; }
    public boolean isTemBeta() { return temBeta; }

    @Override
    public String toString() {
        return super.toString() +
                String.format(" | Cafeína: %dmg | Beta-Alanina: %s",
                        cafeina, temBeta ? "Sim" : "Não");
    }
}