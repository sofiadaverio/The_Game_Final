package ar.ed.unlu.modelo;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Juego extends ObservableRemoto implements IJuego  {
    private ArrayList<Mazo> mazos;
    private MazoPrincipal mazoPrincipal;
    private Equipo equipo;
    private Integer turnoActual = 0;
    private EstadoJuego estadoJuego;
    private List<String> mensajes;

    public Juego() {
        this.estadoJuego = EstadoJuego.ESPERANDO;
        this.equipo = new Equipo();
        this.mensajes = new ArrayList<>();
        this.mazos = new ArrayList<>();
        this.mazoPrincipal = new MazoPrincipal();
    }

    @Override
    public void iniciarJuego() throws RemoteException {
        // --- VALIDACIÓN DE JUGADORES ---
        if (this.equipo.getJugadores().size() < 2) {
            throw new RemoteException("Se necesitan mínimo 2 jugadores.");
        }

        // Reinicio completo
        this.mazos = new ArrayList<>();
        this.mazos.add(new Mazo(TipoMazo.ASCENDENTE));
        this.mazos.add(new Mazo(TipoMazo.DESCENDENTE));
        this.mazoPrincipal = new MazoPrincipal();

        for(Jugador jugador : this.equipo.getJugadores()) {
            jugador.getMano().clear();
            jugador.robarCarta(this.mazoPrincipal);
        }

        this.estadoJuego = EstadoJuego.EN_PROCESO;
        this.turnoActual = 0;
        notificarObservadores("JUEGO_INICIADO");
    }

    @Override
    public boolean jugarTurno(Carta cartaQueVino, Mazo mazoQueVino) throws RemoteException {
        if (this.estadoJuego != EstadoJuego.EN_PROCESO) return false;

        // 1. BUSCAR MAZO REAL
        Mazo mazoReal = null;
        for (Mazo m : this.mazos) {
            if (m.getTipoMazo() == mazoQueVino.getTipoMazo()) {
                mazoReal = m;
                break;
            }
        }
        if (mazoReal == null) return false;

        // 2. BUSCAR CARTA REAL
        Jugador jugadorActual = this.equipo.obtenerJugadorActual(this.turnoActual);
        Carta cartaReal = null;

        for (Carta c : jugadorActual.getMano()) {
            if (c.getColor() == cartaQueVino.getColor() &&
                    c.getNumero().intValue() == cartaQueVino.getNumero().intValue()) {
                cartaReal = c;
                break;
            }
        }

        if (cartaReal == null) return false;

        // 3. JUGAR
        try {
            jugadorActual.colocarCarta(cartaReal, mazoReal);
            this.verificarFin(); // Chequeamos si ganaron o perdieron
            notificarObservadores("JUGADA_EXITOSA");
            return true;

        } catch (Exception e) {
            System.err.println("Error jugada: " + e.getMessage());
            return false;
        }
    }

    // --- MÉTODOS STANDARD ---

    @Override
    public void conectarJugador(String nombre) throws RemoteException {
        for(Jugador j : equipo.getJugadores()) {
            if(j.getNombre().equals(nombre)) return;
        }
        this.equipo.agregarJugador(new Jugador(nombre));
        notificarObservadores("JUGADOR_CONECTADO");
    }

    @Override
    public void terminarTurno() throws RemoteException {
        Jugador jugadorActual = this.getJugadorActual();
        jugadorActual.robarCarta(this.mazoPrincipal);
        this.pasarTurno();
        this.verificarFin(); // Chequeamos si perdieron al no poder robar o moverse
        notificarObservadores("TURNO_TERMINADO");
    }

    @Override
    public void desconectarJugador(String nombre) throws RemoteException { }

    @Override
    public Jugador getJugadorActual() throws RemoteException {
        if (this.equipo.getJugadores().isEmpty()) return null;
        return this.equipo.obtenerJugadorActual(this.turnoActual);
    }

    @Override
    public List<Mazo> getMazos() throws RemoteException {
        return this.mazos;
    }

    @Override
    public List<Carta> getCartasJugador(String nombre) throws RemoteException {
        for (Jugador j : equipo.getJugadores()) {
            if (j.getNombre().equals(nombre)) return j.getMano();
        }
        return new ArrayList<>();
    }

    @Override
    public EstadoJuego getEstadoJuego() throws RemoteException {
        return this.estadoJuego;
    }

    @Override
    public List<String> getNotificaciones() throws RemoteException {
        return this.mensajes;
    }

    @Override
    public List<String> getNombresJugadores() throws RemoteException {
        List<String> nombres = new ArrayList<>();
        for (Jugador j : equipo.getJugadores()) nombres.add(j.getNombre());
        return nombres;
    }

    @Override
    public boolean tieneMovimientoValidos(Jugador jugador) throws RemoteException {
        return jugador.tieneMovimientosValidos(this.mazos);
    }

    public void verificarFin() throws RemoteException {
        if (this.mazoPrincipal.isVacio() && this.equipo.getJugadores().stream().allMatch((j) -> j.getMano().isEmpty())) {
            this.estadoJuego = EstadoJuego.GANADO;
            notificarObservadores("JUEGO_GANADO");
        } else if (!this.equipo.hayJugadoresConMovimientos(this.mazos)) {
            this.estadoJuego = EstadoJuego.PERDIDO;
            notificarObservadores("JUEGO_PERDIDO");
        }
    }

    @Override
    public void transmisi0nMensaje (Mensajes mensaje, String emisor) throws RemoteException {
        // Formato: CHAT:Nombre: Mensaje
        // Usamos el prefijo "CHAT:" para que el controlador sepa que NO es una jugada de cartas
        String textoParaEnviar = "CHAT:" + emisor + ": " + mensaje.getMensaje();

        // Notificamos a todos los observadores (Controladores)
        notificarObservadores(textoParaEnviar);
    }

    public int pasarTurno() {
        if (this.equipo.getJugadores().isEmpty()) return 0;
        this.turnoActual = (this.turnoActual + 1) % this.equipo.getJugadores().size();
        return this.turnoActual;
    }
}