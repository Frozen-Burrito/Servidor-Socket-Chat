import javax.swing.SwingUtilities;

import com.pasarceti.chat.servidor.controladores.ServidorChat;
import com.pasarceti.chat.servidor.logeventos.LoggerDeEventos;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        ServidorChat servidor = new ServidorChat(9998, Level.INFO);
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
        // Crear un logger que consuma los eventos producidos por el servidor.
        LoggerDeEventos logEventos = new LoggerDeEventos(servidor.getQueueEventos());

        logEventos.start();
        
        // Ejecutar el servidor en un hilo aparte.
        Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //TODO: Iniciar la ejecución del GUI.
//                gui.show(); // Quizas algo asi?
            }
        });
    }
}
