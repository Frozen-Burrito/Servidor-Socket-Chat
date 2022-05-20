package com.pasarceti.chat.servidor.controladores;

import com.pasarceti.chat.servidor.bd.PoolConexionesBD;
import com.pasarceti.chat.servidor.modelos.Evento;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @brief El servidor que realiza toda la comunicación con los clientes del chat.
*/
public class ServidorChat implements Runnable
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
    
    // El servidor de sockets, usado para aceptar las conexiones.
    private ServerSocket socketServidor;

    // Servicio de logging para el servidor.
    private static final Logger logger = Logger.getLogger("ServidorChat");

    // Mantiene el estado del servidor en tiempo real, notifica a los listeners 
    // registrados cuando cambia alguna de sus propiedades.
    private final EstadoServidor estado = new EstadoServidor();
    
    private boolean detenido;

    // El queue para el patrón productor-consumidor (servidor y gui, en este caso)
    // que envía los eventos producidos por el servidor a los consumidores de este queue.
    private final BlockingQueue<Evento> queueEventos = new LinkedBlockingQueue<>(MAX_EVENTOS_EN_QUEUE);
    
    private final NotificadorDeEventos notificador;
    
    private final PoolConexionesBD poolDeConexiones;
    
    private Thread hiloDeEjecucion;

    public ServidorChat(int puerto, Level nivelDeLogs, PoolConexionesBD poolDeConexiones) 
    {
        this.puerto = puerto;
        logger.setLevel(nivelDeLogs);
        
        this.notificador = new NotificadorDeEventos(estado, queueEventos);
        
        this.poolDeConexiones = poolDeConexiones;
    }
    
    @Override
    public void run() 
    {
        hiloDeEjecucion = Thread.currentThread();
        
        ejecutar();
    }
    
    public void reiniciar()
    {
        hiloDeEjecucion = new Thread(this);
        hiloDeEjecucion.start();
    }
    
    public void interrumpir()
    {
        hiloDeEjecucion.interrupt();
    }
    
    /**
     * @brief Ejecuta el servidor, haciendo que esté disponible y pueda recibir y 
     * enviar datos.
     */
    private void ejecutar() 
    {
        try 
        {
            logger.info(String.format(
                "Inciando servidor de chat en el puerto %s", 
                String.valueOf(puerto)
            ));
            
            // Registrar el servidor como listener de eventos, para poder notificar
            // a los clientes debidos.
            estado.agregarListener(notificador);
            
            socketServidor = new ServerSocket();
            socketServidor.setReuseAddress(true);
            socketServidor.bind(new InetSocketAddress(puerto));
            
            detenido = false;
            
            logger.info("Servidor iniciado, esperando conexiones.");

            // Aceptar conexiones mientras el servicio de ejecucion siga activo.
            while (!exec.isShutdown()) 
            {   
                if (Thread.interrupted())
                {
                    throw new InterruptedException();
                }
                
                // Esperar a que haya una conexion de un cliente. Aceptarla cuando llegue.
                final Socket cliente = socketServidor.accept();

                // Timeout de 10 segundos para los read() al socket.
//                cliente.setSoTimeout(10000);

                logger.info("Cliente conectado");
                
                // Manejar la comunicación del cliente con una instancia de ManejadorClientes.
                Runnable tareaPeticion = new ManejadorCliente(cliente, estado, queueEventos, poolDeConexiones);

                exec.execute(tareaPeticion);
            }        
        }
        catch (SocketException e) 
        {
            // Invocar socketServidor.close() produce esta excepcion, es importante
            // asegurar que hilo donde está corriendo "ejecutar()" se detenga y termine. 
            logger.log(Level.WARNING, "Excepción de socket: {0}", e.getMessage());
        }
        catch (IOException e) 
        {
            logger.log(Level.SEVERE, "Error iniciando el servidor: {0}", e.getMessage());
            detener();
        } catch (InterruptedException ex) 
        {
            System.out.println("Servidor interrumpido");
            detener();
            logger.info("Servidor detenido con interrupción.");
        }
        finally 
        {
            logger.info("El servidor fue desactivado; no se aceptan más conexiones.");
            estado.removerListener(notificador);
        }
    }
    
    public boolean estaDetenido()
    {
        return detenido;
    }
    
    /**
     * Cierra el servidor de sockets, interrumpiendo cualquier comunicación pendiente.
     */
    private void detener()
    {
        if (socketServidor != null) 
        {
            try 
            {
                socketServidor.close();
                detenido = true;
                
            } catch (IOException e) 
            {
                logger.log(
                    Level.SEVERE, 
                    "Ocurrió un error al detener el servidor: {0}", 
                    e.getMessage()
                );
            }
        }
    }

    /**
     * Intenta detener completamente al servidor "con gracia", evitando que acepte nuevas 
     * conexiones pero esperando a que las conexiones existentes terminen de 
     * ser procesadas.
     * 
     * @return <strong>true</strong> si el servidor fue detenido y todas las 
     * conexiones restantes fueron procesadas.
    */
    public boolean terminar() 
    {
        try 
        {            
            exec.shutdown();
            boolean execDetenidoSinTimeout = exec.awaitTermination(30, TimeUnit.SECONDS);
            
            if (socketServidor != null) 
            {
                socketServidor.close();
            }
            
            logger.info(execDetenidoSinTimeout 
                ? "Servidor terminado" 
                : "Tiempo de espera excedido al intentar terminar el servidor"
            );
            
            return exec.isTerminated();
            
        } catch (InterruptedException | IOException e) 
        {
            logger.log(
                Level.SEVERE, 
                "Ocurri\u00f3 un error al intentar terminar el servidor: {0}", 
                e.getMessage()
            );
        }
        
        return false;
    }

    public BlockingQueue<Evento> getQueueEventos()
    {
        return queueEventos;
    }
}
