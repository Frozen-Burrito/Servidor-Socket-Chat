package com.pasarceti.chat.servidor;

import javax.swing.SwingUtilities;

import com.pasarceti.chat.servidor.controladores.ServidorChat;
import com.pasarceti.chat.servidor.bd.CredencialesBD;
import com.pasarceti.chat.servidor.bd.PoolConexionesBD;
import com.pasarceti.chat.servidor.gui.InterfazGrafica;
import com.pasarceti.chat.servidor.gui.WorkerEventos;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        CredencialesBD credenciales = null;
        
        try 
        {
            credenciales = CredencialesBD.desdeArchivoConfig("config_servidor.txt");
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        PoolConexionesBD poolDeConexiones = new PoolConexionesBD(credenciales);
        
        ServidorChat servidor = new ServidorChat(9998, Level.INFO, poolDeConexiones);
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
        // Ejecutar el servidor en un hilo aparte.
        final Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();
        
        InterfazGrafica gui = new InterfazGrafica();
        
        gui.ConfigurarBtnON((ActionEvent e) -> {
            if (servidor.estaDetenido())
            {
                gui.ActBtnOn();
                final Thread hiloServidorReactivado = new Thread(servidor);
                hiloServidorReactivado.start();
            }
        });
        
        gui.ConfigurarBtnOFF((ActionEvent e) -> {
            gui.ActBtnOFF();
            hiloServidor.interrupt();
        });
        
        WorkerEventos workerEventos = new WorkerEventos(servidor.getQueueEventos(), gui);
        
        Evento notificacion = new Evento(
            TipoDeEvento.AMISTAD_ACEPTADA,
            new EventoServidor(
                TipoDeEvento.AMISTAD_ACEPTADA, 
                "Esto es el JSON"
            )
        );
        
        System.out.println(notificacion.toString());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              // Iniciar a ejecutar la interfaz
              gui.Inicio();
              workerEventos.execute();
              gui.setVisible(true);
            }
        });
    }
}
