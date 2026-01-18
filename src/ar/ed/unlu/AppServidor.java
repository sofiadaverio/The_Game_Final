package ar.ed.unlu; // O el paquete donde lo crees

import ar.ed.unlu.modelo.Juego;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;

public class AppServidor {

    public static void main(String[] args) {
        try {
            // 1. Preguntamos puerto (8888 por defecto)
            String puerto = (String) JOptionPane.showInputDialog(
                    null, "Puerto del Servidor:", "Configurar Server",
                    JOptionPane.QUESTION_MESSAGE, null, null, "8888");

            int port = Integer.parseInt(puerto);

            // 2. Instanciamos el Modelo Real
            Juego modelo = new Juego();

            // 3. Iniciamos el Servidor de la UNLu
            Servidor servidor = new Servidor("127.0.0.1", port);
            System.out.println("Iniciando servidor en puerto " + port + "...");

            servidor.iniciar(modelo);

            System.out.println("¡Servidor OK! Esperando clientes...");

        } catch (RemoteException | RMIMVCException e) {
            e.printStackTrace();
        }
    }
}