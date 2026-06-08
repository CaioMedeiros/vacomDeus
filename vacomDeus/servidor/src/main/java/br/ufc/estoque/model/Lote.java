package br.ufc.estoque.model;

import java.time.LocalDate;

public class Lote {

    private String    codigo;
    private int       quantidade;
    private LocalDate dataFabricacao;
    private LocalDate dataVencimento;

    public Lote() {}

    public Lote(String codigo, int quantidade, LocalDate dataFabricacao, LocalDate dataVencimento) {
        this.codigo         = codigo;
        this.quantidade     = quantidade;
        this.dataFabricacao = dataFabricacao;
        this.dataVencimento = dataVencimento;
    }

    public String    getCodigo()          { return codigo; }
    public void      setCodigo(String c)  { this.codigo = c; }
    public int       getQuantidade()      { return quantidade; }
    public void      setQuantidade(int q) { this.quantidade = q; }
    public LocalDate getDataFabricacao()  { return dataFabricacao; }
    public void      setDataFabricacao(LocalDate d) { this.dataFabricacao = d; }
    public LocalDate getDataVencimento()  { return dataVencimento; }
    public void      setDataVencimento(LocalDate d) { this.dataVencimento = d; }
}
