package ar.ed.unlu.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import java.rmi.RemoteException;
import java.util.List;

public interface IJuego extends IObservableRemoto {

    void iniciarJuego() throws RemoteException;

    boolean jugarTurno(Carta carta, Mazo mazo) throws RemoteException;

    void conectarJugador(String nombre) throws RemoteException;

    // ESTOS SON LOS QUE TE FALTAN:
    void terminarTurno() throws RemoteException;

    void desconectarJugador(String nombre) throws RemoteException;

    Jugador getJugadorActual() throws RemoteException;

    List<Mazo> getMazos() throws RemoteException;

    List<Carta> getCartasJugador(String nombre) throws RemoteException;

    EstadoJuego getEstadoJuego() throws RemoteException;

    List<String> getNotificaciones() throws RemoteException;

    List<String> getNombresJugadores() throws RemoteException;

    boolean tieneMovimientoValidos(Jugador jugador) throws RemoteException;

    void transmisi0nMensaje(Mensajes mensaje, String emisor) throws RemoteException;
}