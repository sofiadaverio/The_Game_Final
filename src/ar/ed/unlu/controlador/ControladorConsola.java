package ar.ed.unlu.controlador;

import ar.ed.unlu.modelo.*;
import ar.ed.unlu.vista.consola.EstadoVistaConsola;
import ar.ed.unlu.vista.consola.VistaConsola;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorConsola implements IControladorRemoto {

    private IJuego juego;
    private Map<String, VistaConsola> vistasPorJugador;
    private EstadoTurno estadoTurno;
    private String mensajeFeedback = "";
    public ControladorConsola() {
        this.vistasPorJugador = new HashMap<>();
        this.estadoTurno = EstadoTurno.PRIMER_CARTA;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modeloRemoto) throws RemoteException {
        this.juego = (IJuego) modeloRemoto;
    }

    @Override
    public void actualizar(IObservableRemoto modelo, Object evento) throws RemoteException {
        // 1. Verificamos si es un mensaje de CHAT
        if (evento instanceof String) {
            String texto = (String) evento;
            if (texto.startsWith("CHAT:")) {
                // Es un chat: Quitamos el prefijo "CHAT:" y mostramos solo el texto
                String mensajeLimpio = texto.substring(5); // Borra los primeros 5 caracteres

                // Se lo mandamos a TODAS las vistas conectadas a este controlador
                for (VistaConsola vista : vistasPorJugador.values()) {
                    vista.mostrarMensajeChat(mensajeLimpio);
                }
                return; // IMPORTANTE: Salimos para NO redibujar/borrar la mesa
            }
        }

        // 2. Si no fue chat, asumimos que es una jugada y actualizamos la mesa
        this.actualizarMesa();
    }

    public void agregarVistaJugador(String nombre, VistaConsola vista) {
        vistasPorJugador.put(nombre, vista);
        try {
            this.juego.conectarJugador(nombre);
            vista.iniciar();
        } catch (RemoteException e) {
            vista.mostrarMensaje("Error al conectar: " + e.getMessage());
        }
    }

    private void actualizarMesa() {
        try {
            EstadoJuego estadoGeneral = this.juego.getEstadoJuego();

            // --- 1. SALA DE ESPERA ---
            if (estadoGeneral == EstadoJuego.ESPERANDO) {
                List<String> nombres = this.juego.getNombresJugadores();
                for (VistaConsola vista : this.vistasPorJugador.values()) {
                    vista.setEstado(EstadoVistaConsola.MENU_PRINCIPAL);
                    vista.limpiarPantalla();
                    vista.mostrarMensaje("=== SALA DE ESPERA ===");
                    vista.mostrarMensaje("Conectados: " + nombres.size() + "/5");
                    for (String n : nombres) vista.mostrarMensaje(" - " + n);
                    vista.mostrarMensaje("\n1. Iniciar Partida");
                    vista.mostrarMensaje("2. Ver Reglas");

                    if (!mensajeFeedback.isEmpty()) {
                        vista.mostrarMensaje("\n*** " + mensajeFeedback + " ***");
                    }
                }
                mensajeFeedback = "";
                return;
            }

            // --- 2. FIN DEL JUEGO ---
            if (estadoGeneral == EstadoJuego.GANADO || estadoGeneral == EstadoJuego.PERDIDO) {
                for (VistaConsola vista : this.vistasPorJugador.values()) {
                    vista.limpiarPantalla();
                    if (estadoGeneral == EstadoJuego.GANADO) {
                        vista.mostrarMensaje("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                        vista.mostrarMensaje("        ¡¡ VICTORIA !!          ");
                        vista.mostrarMensaje("   Han vencido a The Game       ");
                        vista.mostrarMensaje("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    } else {
                        vista.mostrarMensaje("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                        vista.mostrarMensaje("      ¡¡ FIN DEL JUEGO !!       ");
                        vista.mostrarMensaje(" No quedan movimientos válidos. ");
                        vista.mostrarMensaje("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    }
                    vista.mostrarMensaje("(Cierra la ventana para salir)");
                }
                return;
            }

            // --- 3. JUEGO EN PROCESO ---
            for (VistaConsola vista : this.vistasPorJugador.values()) {
                vista.activarModoJuego();
            }
            List<Mazo> mazos = this.juego.getMazos();
            Jugador jugadorActualObj = this.juego.getJugadorActual();
            if (jugadorActualObj == null) return;
            String nombreActual = jugadorActualObj.getNombre();

            for (String nombreVista : this.vistasPorJugador.keySet()) {
                VistaConsola vista = this.vistasPorJugador.get(nombreVista);

                if (nombreVista.equals(nombreActual) && this.estadoTurno == EstadoTurno.CONSULAR_MOVIMIENTO) {
                    vista.setEstado(EstadoVistaConsola.SEGUNDA_CARTA);
                } else {
                    vista.setEstado(EstadoVistaConsola.TURNO_JUGADOR);
                }

                vista.limpiarPantalla();
                if (nombreVista.equals(nombreActual)) {
                    vista.mostrarMensaje(">>> ES TU TURNO <<<");
                } else {
                    vista.mostrarMensaje("Turno de: " + nombreActual);
                }

                // CARTAS
                List<Carta> mano = this.juego.getCartasJugador(nombreVista);
                vista.mostrarMensaje("\n--- TUS CARTAS ---");
                if (mano.isEmpty()) vista.mostrarMensaje("(Sin cartas)");
                for (Carta c : mano) {
                    vista.mostrarMensaje(c.getColor() + " " + c.getNumero());
                }

                // MAZOS
                vista.mostrarMensaje("\n--- MAZOS CENTRALES ---");
                if (mazos.isEmpty()) {
                    vista.mostrarMensaje("(Cargando mazos...)");
                } else {
                    for (Mazo m : mazos) {
                        Carta tope = m.obtenerUltimaCarta();
                        String infoTope = (tope == null) ? "Vacío" : (tope.getColor() + " " + tope.getNumero());
                        vista.mostrarMensaje(m.getTipoMazo() + ": " + infoTope);
                    }
                }

                // ACCIONES Y MENSAJES
                if (nombreVista.equals(nombreActual)) {
                    vista.mostrarMensaje("\n--------------------------------");

                    // AQUI MOSTRAMOS EL MENSAJE DE ÉXITO O ERROR
                    if (!mensajeFeedback.isEmpty()) {
                        vista.mostrarMensaje(">> " + mensajeFeedback);
                        vista.mostrarMensaje("--------------------------------");
                    }

                    if (this.estadoTurno == EstadoTurno.CONSULAR_MOVIMIENTO) {
                        vista.mostrarMensaje("¿Quieres jugar otra carta? (si/no)");
                    } else {
                        vista.mostrarMensaje("Ingresa jugada: Color Numero Mazo");
                    }
                }
            }
            // Limpiamos el mensaje para que no salga eternamente
            mensajeFeedback = "";

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void procesarMenuPrincipal(String entrada, String nombreJugador) {
        try {
            if (entrada.equals("1")) {
                this.juego.iniciarJuego();
            } else if (entrada.equals("2")) {
                mensajeFeedback = "Reglas: Ascendente sube (1->99), Descendente baja (100->2).";
                this.actualizarMesa();
            }
        } catch (RemoteException e) {
            notificarVista(nombreJugador, "Error: " + e.getMessage());
        }
    }

    public void procesarTurnoJugador(String entrada, String nombreJugador) {
        try {
            if (!this.juego.getJugadorActual().getNombre().equals(nombreJugador)) {
                notificarVista(nombreJugador, "No es tu turno.");
                return;
            }
            if (this.estadoTurno == EstadoTurno.CONSULAR_MOVIMIENTO) {
                this.procesarConfirmacionSegundaJugada(entrada, nombreJugador);
            } else {
                this.jugarCarta(entrada, nombreJugador);
            }
        } catch (RemoteException e) { notificarVista(nombreJugador, "Error conexión"); }
    }

    private void jugarCarta(String entrada, String nombreJugador) {
        try {
            String[] partes = entrada.trim().split("\\s+");
            if (partes.length < 3) throw new IllegalArgumentException("Faltan datos (Color Numero Mazo).");

            Carta carta = this.convertirACarta(partes[0], Integer.parseInt(partes[1]));
            Mazo mazo = this.convertirAMazo(partes[2]);

            boolean exito = this.juego.jugarTurno(carta, mazo);

            if (exito) {
                // GUARDAMOS EL MENSAJE PARA QUE SE VEA DESPUÉS DE ACTUALIZAR
                this.mensajeFeedback = "¡Jugada exitosa!";

                if (!this.juego.tieneMovimientoValidos(this.juego.getJugadorActual())) {
                    this.finalizarTurno();
                } else if (this.estadoTurno == EstadoTurno.PRIMER_CARTA) {
                    this.estadoTurno = EstadoTurno.CONSULAR_MOVIMIENTO;
                    this.actualizarMesa();
                } else {
                    this.finalizarTurno();
                }
            } else {
                // Si falla, también usamos feedback para que se vea
                this.mensajeFeedback = "Movimiento inválido (Reglas o carta incorrecta).";
                this.actualizarMesa();
            }
        } catch (Exception e) {
            this.mensajeFeedback = "Error: " + e.getMessage();
            try { this.actualizarMesa(); } catch (Exception ex) {}
        }
    }

    public void procesarConfirmacionSegundaJugada(String entrada, String nombreJugador) {
        // AHORA ES ESTRICTO: SOLO "si" O "no"
        if (entrada.trim().equalsIgnoreCase("si")) {
            this.estadoTurno = EstadoTurno.SEGUNDA_CARTA;
            this.mensajeFeedback = "Juega tu segunda carta.";
            this.actualizarMesa();
        } else if (entrada.trim().equalsIgnoreCase("no")) {
            this.finalizarTurno();
        } else {
            this.mensajeFeedback = "Opción no válida. Escribe 'si' o 'no'.";
            this.actualizarMesa();
        }
    }

    private void finalizarTurno() {
        try {
            this.juego.terminarTurno();
            this.estadoTurno = EstadoTurno.PRIMER_CARTA;
            // No seteamos mensajeFeedback aquí para que arranque limpio el otro turno
            this.actualizarMesa();
        } catch (RemoteException e) { e.printStackTrace(); }
    }

    private Carta convertirACarta(String entradaColor, int numero) {
        String c = entradaColor.trim().toLowerCase();
        ColorCarta color = null;
        if (c.startsWith("r")) color = ColorCarta.ROJA;
        else if (c.startsWith("v")) color = ColorCarta.VERDE;
        else if (c.startsWith("az")) color = ColorCarta.AZUL;
        else if (c.startsWith("am")) color = ColorCarta.AMARILLO;
        else if (c.startsWith("g")) color = ColorCarta.GRIS;

        if (color == null) {
            try { color = ColorCarta.valueOf(entradaColor.toUpperCase()); }
            catch (Exception e) { throw new IllegalArgumentException("Color desconocido (usa rojo, verde, azul, amarillo, gris)."); }
        }
        return new Carta(numero, color);
    }

    private Mazo convertirAMazo(String entradaMazo) throws RemoteException {
        List<Mazo> mazos = this.juego.getMazos();
        String m = entradaMazo.trim().toLowerCase();

        for (Mazo mazo : mazos) {
            boolean esAsc = mazo.getTipoMazo() == TipoMazo.ASCENDENTE;
            boolean esDes = mazo.getTipoMazo() == TipoMazo.DESCENDENTE;

            if (esAsc && (m.equals("asc") || m.equals("a"))) return mazo;

            if (esDes && (m.equals("des") || m.equals("d"))) return mazo;

            if (mazo.getTipoMazo().toString().toLowerCase().startsWith(m)) return mazo;
        }
        throw new IllegalArgumentException("Mazo incorrecto. Usa: 'asc' o 'a' / 'des' o 'd'.");
    }

    private void notificarVista(String nombre, String msj) {
        if (vistasPorJugador.containsKey(nombre)) vistasPorJugador.get(nombre).mostrarMensaje(msj);
    }

    public void enviarMensajeChat(Mensajes mensaje, String nombreJugador) {
        try {
            // Le pasamos el mensaje y quién lo manda
            this.juego.transmisi0nMensaje(mensaje, nombreJugador);
        } catch (RemoteException e) {
            // Si falla, mostramos el error en la vista de ese jugador
            if (vistasPorJugador.containsKey(nombreJugador)) {
                vistasPorJugador.get(nombreJugador).mostrarMensaje("Error al enviar chat: " + e.getMessage());
            }
        }
    }
}