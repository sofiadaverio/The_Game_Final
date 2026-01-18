package ar.ed.unlu.modelo;

import java.util.Stack;

public class Mazo implements java.io.Serializable{
    private TipoMazo tipoMazo;
    private Stack<Carta> cartas;

    public Mazo(TipoMazo tipo) {
        this.tipoMazo = tipo;
        this.cartas = new Stack<>();
    }

    public void agregarCarta(Carta carta) {
        cartas.push(carta);
    }

    public boolean validarMovimiento(Carta carta) {
        if (cartas.isEmpty()) {
            return true; // Siempre válida si el mazo está vacío
        }
        Carta ultimaCarta = obtenerUltimaCarta();

        // Validación para mazo ascendente
        if (tipoMazo == TipoMazo.ASCENDENTE) {
            return carta.getNumero() > ultimaCarta.getNumero() || carta.getColor() == ultimaCarta.getColor();
        }

        // Validación para mazo descendente
        if (tipoMazo == TipoMazo.DESCENDENTE) {
            return carta.getNumero() < ultimaCarta.getNumero() || carta.getColor() == ultimaCarta.getColor();
        }

        return false; // Por defecto, movimiento inválido
    }

    public Carta obtenerUltimaCarta(){
        if (!cartas.isEmpty()) {
            return cartas.peek(); // obtener la referencia del elemento que se encuentra en la parte superior de la pila
        }
        return null; // obtener la referencia del elemento que se encuentra en la parte superior de la pila
    }

    public TipoMazo getTipoMazo() {
        return tipoMazo;
    }
}
