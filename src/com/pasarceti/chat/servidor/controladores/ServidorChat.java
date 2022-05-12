package com.pasarceti.chat.servidor.controladores;

import com.pasarceti.chat.servidor.modelos.Evento;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @brief El servidor que realiza toda la comunicación con los clientes del chat.
*/
public class ServidorChat
{
    // El puerto del sistema en que está disponible este servidor.
    private final int puerto; 

    // La cantidad de hilos que va a utilizar esta instancia de servidor.
    private static final int NUM_HILOS = 100;

    // La cantidad máxima de eventos que pueden estar en queueEventos a la vez.
    private static final int MAX_EVENTOS_EN_QUEUE = 100;

    // Este Executor coordina y ejecuta todas las tareas de procesamiento de 
    // comunicación con sockets.
    private static final ExecutorService exec = Executors.newFixedThreadPool(NUM_HILOS);  

    // Servicio de logging para el servidor.
    private static final Logger logger = Logger.getLogger("ServidorChat");

    // Mantiene el estado del servidor en tiempo real, notifica a los listeners 
    // registrados cuando cambia alguna de sus propiedades.
    private final EstadoServidor estado = new EstadoServidor();

    // El queue para el patrón productor-consumidor (servidor y gui, en este caso)
    // que envía los eventos producidos por el servidor a los consumidores de este queue.
    private final BlockingQueue<Evento> queueEventos = new LinkedBlockingQueue<>(MAX_EVENTOS_EN_QUEUE);

    public ServidorChat(int puerto) 
    {
        this.puerto = puerto;
    }
    
    /**
    * @brief Ejecuta el servidor, haciendo que esté disponible y pueda recibir y 
    * enviar datos.
    */
    public void ejecutar() 
    {
        try 
        {
            logger.info("Inciando servidor de chat...");

            // Crear una nueva instancia de un ServerSocket.
            ServerSocket socketServidor = new ServerSocket(puerto);

            logger.info("Servidor iniciado, esperando conexiones.");

            // Aceptar conexiones mientras el servicio de ejecucion siga activo.
            while (!exec.isShutdown()) 
            {   
                // Esperar a que haya una conexion de un cliente. Aceptarla cuando llegue.
                final Socket cliente = socketServidor.accept();

                // Timeout de 10 segundos para los read() al socket.
//                cliente.setSoTimeout(10000);

                logger.info("Cliente conectado");

                // Crear un nuevo runnable para ejecutar el manejo de la comunicacion 
                // en otro hilo.
//                Runnable tareaPeticion = new HiloServidor(cliente, queueEventos);
                Runnable tareaPeticion = new HiloServidor(cliente, estado, queueEventos);

                // Ejecutar la tarea de manejo de la peticion, a traves del Executor.
                exec.execute(tareaPeticion);
            }

            logger.info("El servidor ya dejo de esperar conexiones.");        }
        catch (IOException e) 
        {
            logger.log(Level.SEVERE, "Error iniciando el servidor: {0}", e.getMessage());
            detener();
        }
    }

    /**
     * Detiene la ejecucion del servidor. 
    */
    public void detener() 
    {
        exec.shutdown();
        logger.info("Servidor detenido");
    }

    public BlockingQueue<Evento> getQueueEventos()
    {
        return queueEventos;
    }
}
