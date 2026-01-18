package ar.ed.unlu;

import ar.ed.unlu.controlador.ControladorConsola;
import ar.ed.unlu.vista.consola.VistaConsola;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.cliente.Cliente;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;

public class AppCliente {

    public static void main(String[] args) {
        try {
            // CONFIGURACIÓN DE RED (TU CÓDIGO ORIGINAL)

            String ipServidor = "127.0.0.1";

            String portServidorStr = (String) JOptionPane.showInputDialog(
                    null, "Puerto del Servidor:", "Conectar",
                    JOptionPane.QUESTION_MESSAGE, null, null, "8888");
            int portServidor = Integer.parseInt(portServidorStr);

            String ipCliente = "127.0.0.1";

            String portClienteStr = (String) JOptionPane.showInputDialog(
                    null, "Puerto LOCAL:", "Conectar",
                    JOptionPane.QUESTION_MESSAGE, null, null, "9990");
            int portCliente = Integer.parseInt(portClienteStr);

            //  INICIO DE COMPONENTES

            ControladorConsola controlador = new ControladorConsola();

            Cliente cliente = new Cliente(ipCliente, portCliente, ipServidor, portServidor);
            System.out.println("Iniciando cliente...");
            cliente.iniciar(controlador);

            String nombre = JOptionPane.showInputDialog("Tu nombre de jugador:");

            // SELECCIÓN DE VISTA (LO NUEVO QUE PEDISTE)

            String[] opciones = {"Consola (Texto)", "Gráfica (Ventanas)"};
            int seleccion = JOptionPane.showOptionDialog(
                    null,
                    "¿Con qué interfaz quieres jugar?",
                    "Selector de Vista",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            //  LÓGICA DE SELECCIÓN
            if (seleccion == 1) {
                // OPCIÓN GRÁFICA (Opción 2)
                JOptionPane.showMessageDialog(null, "La vista gráfica se está configurando.\nAbriendo modo Consola temporalmente...");

                // --- AQUÍ PONDREMOS LA VISTA GRÁFICA CUANDO ESTÉ LISTA ---
                // VistaConsolaGrafica vistaGrafica = new VistaConsolaGrafica(nombre, controlador);
                // controlador.agregarVistaJugador(nombre, vistaGrafica);

                // Por ahora, fallback a consola:
                VistaConsola vista = new VistaConsola(nombre, controlador);
                controlador.agregarVistaJugador(nombre, vista);
                iniciarVista(vista);

            } else {
                // OPCIÓN CONSOLA (Opción 1 - La que funciona)
                VistaConsola vista = new VistaConsola(nombre, controlador);
                controlador.agregarVistaJugador(nombre, vista);
                iniciarVista(vista);
            }

        } catch (RemoteException | RMIMVCException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar para iniciar la ventana (Tu código original ordenado)
    private static void iniciarVista(VistaConsola vista) {
        System.out.println("Abriendo ventana...");
        vista.setVisible(true);
        vista.toFront();
        vista.requestFocus();
    }
}