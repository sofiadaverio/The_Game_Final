package ar.ed.unlu.vista.consola;

import ar.ed.unlu.controlador.ControladorConsola;
import ar.ed.unlu.modelo.Mensajes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VistaConsola extends JFrame {
    private JPanel panelPrincipal;

    // --- LADO IZQUIERDO (JUEGO) ---
    private JScrollPane scrol;
    private JTextArea txtSalida;
    private JTextField txtEntrada;

    // --- LADO DERECHO (CHAT)
    private JScrollPane scroll2;
    private JTextArea txtChat;
    private JComboBox<Mensajes> comboBox;
    private JButton enviarButton;

    private ControladorConsola controlador;
    private EstadoVistaConsola estado;
    private String nombreJugador;

    // Para evitar recargar el combo a cada rato
    private boolean modoJuegoActivo = false;

    private void createUIComponents() {
        // 1. IZQUIERDA
        txtEntrada = new JTextField();
        txtSalida = new JTextArea();
        txtSalida.setEditable(false);
        scrol = new JScrollPane(txtSalida);

        // 2. DERECHA
        txtChat = new JTextArea();
        txtChat.setEditable(false);
        scroll2 = new JScrollPane(txtChat);

        // 3. CONTROLES (Inicializarlos aquí evita errores si el .form se confunde)
        comboBox = new JComboBox<>();
        enviarButton = new JButton("Enviar");
    }

    public VistaConsola(String nombreJugador, ControladorConsola controlador) {
        this.nombreJugador = nombreJugador;
        this.controlador = controlador;

        setTitle("The Game - Jugador: " + nombreJugador);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Un poco más ancho para que entren los dos paneles
        setLocationRelativeTo(null);

        if (panelPrincipal != null) {
            setContentPane(panelPrincipal);
        } else {
            // Fallback de emergencia
            System.err.println("Error: panelPrincipal es null. Revisa el .form");
            // (Si pasa esto, el createUIComponents ayuda, pero el diseño se rompería)
        }

        // 1. CONFIGURAR ACCIONES DE JUEGO (Izquierda)
        txtEntrada.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!txtEntrada.getText().isEmpty()) {
                    procesarEntrada(txtEntrada.getText());
                    txtEntrada.setText("");
                }
            }
        });

        // 2. CONFIGURAR ACCIONES DE CHAT (Derecha)
        configurarChat();

        // 3. INICIAR EN MODO LOBBY
        mostrarMenuPrincipal();
    }

    private void procesarEntrada(String entrada) {
        entrada = entrada.trim();
        // Opcional: mostrar lo que uno escribe (eco)
        // mostrarMensaje("> " + entrada);

        switch (estado) {
            case MENU_PRINCIPAL:
                // Ahora esto maneja "1. Iniciar", "2. Reglas"
                controlador.procesarMenuPrincipal(entrada, nombreJugador);
                break;

            case TURNO_JUGADOR:
                controlador.procesarTurnoJugador(entrada, nombreJugador);
                break;

            case SEGUNDA_CARTA:
                // Aquí solo esperamos un "si" o "no"
                controlador.procesarConfirmacionSegundaJugada(entrada, nombreJugador);
                break;

            case COMUNICACION:
                // Si agregas chat después, iría acá
                break;
        }
    }

    // --- MENÚ UNIFICADO (Ya no hay "Agregar Jugador") ---
    public void mostrarMenuPrincipal() {
        this.estado = EstadoVistaConsola.MENU_PRINCIPAL;

        actualizarOpcionesChat(false);

        limpiarPantalla(); // Limpia izquierda

        if (txtChat.getText().isEmpty()) {
            mostrarMensajeChat("=== CHAT DE SALA ===");
            mostrarMensajeChat("Usa el desplegable para saludar.");
        }
        mostrarMensaje("=== SALA DE ESPERA ===");
        mostrarMensaje("Bienvenido, " + nombreJugador);
        mostrarMensaje("Esperando a otros jugadores...");
        mostrarMensaje("-----------------------------");
        mostrarMensaje("1. Iniciar Partida (Votar para empezar)");
        mostrarMensaje("2. Ver Reglas");
        mostrarMensaje("-----------------------------");
        mostrarMensaje("Escribe el número y dale Enter/Click:");
    }

    public void mostrarMensaje(String mensaje) {
        txtSalida.append(mensaje + "\n");
        // Auto-scroll hacia abajo
        txtSalida.setCaretPosition(txtSalida.getDocument().getLength());
    }

    public void limpiarPantalla(){
        txtSalida.setText("");
    }

    public void iniciar() {
        setVisible(true);
        toFront();
        txtEntrada.requestFocus();
    }

    // --- Getters y Setters ---

    public void setEstado(EstadoVistaConsola estado) {
        this.estado = estado;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    private void configurarChat() {
        // Acción del botón ENVIAR
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensajeSeleccionado();
            }
        });
    }

    private void enviarMensajeSeleccionado() {
        Mensajes mensaje = (Mensajes) comboBox.getSelectedItem();
        if (mensaje != null) {
            // Manda el mensaje a través del controlador
            controlador.enviarMensajeChat(mensaje, this.nombreJugador);
        }
    }


    // 2. Muestra cosas del CHAT
    public void mostrarMensajeChat(String mensaje) {
        txtChat.append(mensaje + "\n");
        txtChat.setCaretPosition(txtChat.getDocument().getLength());
    }

    // --- LÓGICA DINÁMICA DEL COMBOBOX ---
    public void actualizarOpcionesChat(boolean esModoJuego) {
        // Solo actualizamos si cambió el modo (para no resetear la selección a cada rato)
        if (this.modoJuegoActivo == esModoJuego && comboBox.getItemCount() > 0) return;

        this.modoJuegoActivo = esModoJuego;
        DefaultComboBoxModel<Mensajes> modelo = new DefaultComboBoxModel<>();

        for (Mensajes m : Mensajes.values()) {
            if (esModoJuego) {
                // Si estamos jugando, mostramos solo las cartas (BAJA, ALTA, ETC)
                if (m.esDeJuego()) modelo.addElement(m);
            } else {
                // Si estamos en lobby, mostramos solo saludos (HOLA, LISTO, ETC)
                if (!m.esDeJuego()) modelo.addElement(m);
            }
        }
        comboBox.setModel(modelo);
    }



    // Método que llamará el controlador cuando empiece el juego
    public void activarModoJuego() {
        // Cambiamos el chat a modo TÁCTICO
        actualizarOpcionesChat(true);
    }

    // Agrega este método en VistaConsola.java
    public void mostrarReglas() {
        System.out.println("\n=================================================================");
        System.out.println("           THE GAME: QUICK & EASY - REGLAMENTO");
        System.out.println("=================================================================");
        System.out.println("OBJETIVO:");
        System.out.println("  - Juegan como un equipo. Deben colocar las 50 cartas en los dos mazos[cite: 6].");
        System.out.println("  - Mazo 1: ASCENDENTE (1 al 10).");
        System.out.println("  - Mazo 2: DESCENDENTE (10 al 1)[cite: 9].");

        System.out.println("\nTURNO DEL JUGADOR:");
        System.out.println("  1. Debes jugar 1 o 2 cartas de tu mano[cite: 7].");
        System.out.println("  2. Repones tu mano al final del turno (vuelves a tener 2 cartas)[cite: 7].");

        System.out.println("\nEL TRUCO DE LA MARCHA ATRÁS (REVERSE):");
        System.out.println("  - Normalmente debes respetar el orden (subir en Ascendente, bajar en Descendente).");
        System.out.println("  - PERO: Si juegas una carta del MISMO COLOR exacto que la que está en la mesa,");
        System.out.println("    puedes ignorar el orden y 'retroceder'[cite: 10].");
        System.out.println("    (Ej: En el mazo Ascendente hay un 7 Verde, puedes jugar un 2 Verde encima).");

        System.out.println("\nCOMUNICACIÓN:");
        System.out.println("  - ¡Hablen entre ustedes!");
        System.out.println("  - PROHIBIDO decir números exactos ('Tengo el 9 rojo')[cite: 75].");
        System.out.println("  - PERMITIDO dar pistas vagas ('Tengo una roja alta', 'No toques el mazo descendente')[cite: 76].");

        System.out.println("\nMODO PROFESIONAL:");
        System.out.println("  - Solo se juega EXACTAMENTE 1 carta por turno (nunca 2)[cite: 83].");
        System.out.println("  - Prohibido dar pistas sobre valores (alto/bajo/medio). Solo colores[cite: 84].");
        System.out.println("=================================================================\n");
    }
}