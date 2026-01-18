package ar.ed.unlu.modelo;

import java.util.ArrayList;
import java.util.Collections;

public class MazoPrincipal implements java.io.Serializable{
    private ArrayList<Carta> cartas;

    public MazoPrincipal(){
        this.cartas = new ArrayList<>();
        generarCartas();
        mezclarCartas();
    }
    private void generarCartas() {
        for( int i = 1; i<=10; i++){
            for(ColorCarta color: ColorCarta.values()){
                cartas.add(new Carta(i,color));
            }
        }
    }
    private void mezclarCartas() {
        Collections.shuffle(cartas);
    }

    public Carta robarCarta() {
        if (this.isVacio()) {
            return null;
        } else {
        return cartas.removeFirst();
    }
    }

    public boolean isVacio() {
        return cartas.isEmpty();
    }

}
