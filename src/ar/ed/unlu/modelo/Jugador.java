package ar.ed.unlu.modelo;

import java.util.ArrayList;
import java.util.List;

public class Jugador implements java.io.Serializable{
    private String nombre;
    private ArrayList<Carta> mano;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mano = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void colocarCarta(Carta carta, Mazo mazo) {
        if (mano.contains(carta)) { // Verifica si la carta está en la mano
            if (mazo.validarMovimiento(carta)) {
                mazo.agregarCarta(carta);
                mano.remove(carta); // Elimina la carta de la mano
            } else {
                throw new IllegalArgumentException("Movimiento inválido. No se puede colocar esta carta en el mazo.");
            }
        } else {
            throw new IllegalStateException("La carta no estaba en la mano del jugador.");
        }
    }

    public void robarCarta(MazoPrincipal mazoPrincipal) {
        while (mano.size() < 2) {
            Carta nuevaCarta = mazoPrincipal.robarCarta();
            if (nuevaCarta != null) {
                mano.add(nuevaCarta);
            } else {
                break;
            }
        }
    }


    public List<Carta> getMano() {
        return mano;
    }

    public boolean tieneMovimientosValidos(List<Mazo> mazos) {
        for (Carta carta : mano) {
            for (Mazo mazo : mazos) {
                if (mazo.validarMovimiento(carta)) {
                    return true;
                }
            }
        }
        return false;
    }
}
