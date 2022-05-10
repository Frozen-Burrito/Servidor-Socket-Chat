package com.pasarceti.chat.servidor.controladores;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    
    // Este Executor coordina y ejecuta todas las tareas de procesamiento de 
    // comunicación con sockets.
    private static final ExecutorService exec = Executors.newFixedThreadPool(NUM_HILOS);  

    // Servicio de logging para el servidor.
    private static final Logger logger = Logger.getLogger("Server");

    // Lista concurrente con los sockets conectados. 
    // Recordar que es más eficiente en iteración que en modificación.
//    private CopyOnWriteArrayList<Socket> clientesConectados = new CopyOnWriteArrayList<>();  

    private final CopyOnWriteArraySet<Observer> observadoresEvt = new CopyOnWriteArraySet<>();

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

                // Crear un nuevo runnable para ejecutar el manejo de la comunicacion 
                // en otro hilo.
                Runnable tareaPeticion = new HiloServidor(cliente);

                if (tareaPeticion instanceof Observable) 
                {
                    // Si la tareaPeticion es un Observable, agregar a todos los 
                    // observadores de eventos al conjunto de observadores de 
                    // tareaPeticion.
                    for (Observer observador : observadoresEvt)
                    {
                        ((Observable) tareaPeticion).addObserver(observador);
                    }
                }

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

/**
 * Agrega un observador de los eventos producidos por el servidor.
 * 
 * @param observer El observador a agregar.
 */
    public void addEventObserver(Observer observer) 
    {
        observadoresEvt.add(observer);
    }
}
