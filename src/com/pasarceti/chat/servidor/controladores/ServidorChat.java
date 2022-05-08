package com.pasarceti.chat.servidor.controladores;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
* @brief El servidor que realiza toda la comunicación con los clientes del chat.
*/
public class ServidorChat 
{
    // El puerto del sistema en que está disponible este servidor.
    private int puerto; 
    
    // La cantidad de hilos que va a utilizar este objeto.
    private static final int NUM_HILOS = 100;
    
    // Este Executor coordina y ejecuta todas las tareas de procesamiento de 
    // comunicación con sockets.
    private static final Executor exec = Executors.newFixedThreadPool(NUM_HILOS);    

    public ServidorChat(int puerto) 
    {
        this.puerto = puerto;
    }
    
    /**
    * @brief Ejecuta el servidor, haciendo que esté disponible y pueda recibir y 
    * enviar datos.
    */
    public void ejecutar() throws IOException 
    {
        // Crear una nueva instancia de un ServerSocket.
        ServerSocket socketServidor = new ServerSocket(puerto);

        while (true) 
        {   
            // Esperar a que haya una conexion de un cliente. Aceptarla cuando llegue.
            final Socket cliente = socketServidor.accept();

            // Crear un nuevo runnable para ejecutar el manejo de la comunicacion 
            // en otro hilo.
            Runnable tareaPeticion = new Runnable()
            {
                @Override
                public void run() 
                {
                    try 
                    {
                        manejarPeticion(cliente);

                    } catch (IOException e) 
                    {
                        System.out.println("Excepcion: " + e.getMessage());
                    }
                }
            };

            // Ejecutar la tarea de manejo de la peticion, a traves del Executor.
            exec.execute(tareaPeticion);
        }
    }

    /**
     * @brief Realiza todas las acciones necesarias para procesar una peticion.

     * - Interpreta la peticion.
     * - Ejecuta la accion solicitada.
     * - Retorna un resultado al cliente.
    */
    private void manejarPeticion(Socket cliente) throws IOException
    {
        //TODO: Manejar peticiones
    }
}
