package com.pasarceti.chat.servidor;

import javax.swing.SwingUtilities;

import com.pasarceti.chat.servidor.controladores.ServidorChat;
import com.pasarceti.chat.servidor.bd.CredencialesBD;
import com.pasarceti.chat.servidor.gui.InterfazGrafica;
import com.pasarceti.chat.servidor.gui.WorkerEventos;
import java.util.logging.Level;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        final CredencialesBD credenciales = new CredencialesBD(
            "jdbc:mysql://localhost/chat",
            "root",
            ""
        );
        
        ServidorChat servidor = new ServidorChat(9998, Level.INFO, credenciales);
/*
        El servidor envía los eventos a una fila FIFO que bloquea. Con esto, otras
        clases o hilos pueden "consumir" los eventos producidos por el servidor.
        
        Para que una clase reciba los eventos del servidor, debería obtener una 
        referencia a la fila de eventos usando servidor.getQueueEventos() y 
        ejecutar el método poll() de la fila:
        
        Evento evento = queueEventos.poll(1, TimeUnit.MINUTES);
        
        Este método bloquea y tiene un timeout, lo que significa que el método 
        retornará un evento cuando esté disponible, o null si se excedió el tiempo
        de espera.
        
        Ver el ejemplo con la clase LoggerDeEventos, que consume los eventos del 
        servidor y los muestra en consola con un Logger.
*/
        
        InterfazGrafica gui = new InterfazGrafica();
        
        WorkerEventos workerEventos = new WorkerEventos(servidor.getQueueEventos(), gui);
        
        // Ejecutar el servidor en un hilo aparte.
        Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              // Iniciar a ejecutar la interfaz
              workerEventos.execute();
              gui.setVisible(true);
            }
        });
    }
}
