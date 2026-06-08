package br.ufc.estoque.model;

public class PreTreino extends Suplemento {

    private int     cafeina;
    private boolean temBetaAlanina;

    public PreTreino() {}

    public PreTreino(String nome, String marca, double preco, int cafeina, boolean temBetaAlanina) {
        super(nome, marca, preco);
        this.cafeina        = cafeina;
        this.temBetaAlanina = temBetaAlanina;
    }

    @Override public String getTipo()                   { return "Pré-Treino"; }
    public int              getCafeina()                { return cafeina; }
    public void             setCafeina(int c)           { this.cafeina = c; }
    public boolean          isTemBetaAlanina()          { return temBetaAlanina; }
    public void             setTemBetaAlanina(boolean b){ this.temBetaAlanina = b; }
}
