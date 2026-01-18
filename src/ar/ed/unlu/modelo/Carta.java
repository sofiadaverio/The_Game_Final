package ar.ed.unlu.modelo;

import java.util.Objects;

public class Carta implements java.io.Serializable{
    private Integer numero;
    private ColorCarta color;

    public Carta(int numero, ColorCarta color){
        this.numero=numero;
        this.color=color;
    }
    public ColorCarta getColor() {
        return color;
    }

    public Integer getNumero() {
        return numero;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Carta carta = (Carta) obj;
        return numero.equals(carta.numero) && color == carta.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, color);
    }



}
