package interfaces;

import java.time.LocalDate;

import java.time.LocalDate;

public interface Validavel {
    boolean estaValido();
    LocalDate getDataVencimento();
    int getDiasParaVencer();
}