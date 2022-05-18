package com.pasarceti.chat.servidor.controladores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.pasarceti.chat.servidor.modelos.AccionCliente;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import java.util.Arrays;

/**
 * Esta clase ejecuta la "tarea" de manejar la comunicacion con los sockets 
 * cliente.
 */
public class ManejadorCliente implements Runnable
{
    // Los milisegundos que puede llegar a bloquear el hilo cuando
    // envía un evento al queueEventos.
    private static final long BLOQUEO_MAX_EVT_MS = 500;

    // El estado del servidor, para subscribirse a sus notificaciones.
    private final EstadoServidor estadoServidor;
    // PROBLEMA: El estado servidor es compartido entre todos los clientes. No puede tener cosas como
    // el ID del usuario actual (porque cada hilo tendra un usuario diferente)

    // El socket con la conexión del cliente.
    private final Socket socket;
    
    // El ID del usuario conectado, es -1 si no ha iniciado sesion.
    private final ThreadLocal<Integer> idUsuario = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return -1;
        }
    };

    // El queue para el patrón productor-consumidor (servidor y gui, en este caso)
    // que envía los eventos producidos por el servidor a los consumidores de este queue.
    private final BlockingQueue<Evento> queueEventos;

    public ManejadorCliente(Socket socket, EstadoServidor estadoServidor, BlockingQueue<Evento> queueEventos) 
    {
        this.socket = socket;
        this.estadoServidor = estadoServidor;
        this.queueEventos = queueEventos;
    }

    @Override
    public void run() 
    {
        try 
        {
            while (!(socket.isClosed()))
            {
                AccionCliente accion = obtenerAccion();
                
                if (accion != null)
                {
                    manejarAccion(accion);
                }
                else 
                {
                    // Si la linea del encabezado fue null, el socket fue desconectado.
                    // Hacer cleanup necesario.
                    System.out.println("El cliente se desconectó, cerrando el socket.");
                    
                    estadoServidor.desconectarUsuario(idUsuario);
                    
                    socket.getInputStream().close();
                    socket.close();

                    System.out.println("Socket cerrado: " + socket.isClosed());
                    System.out.println("Socket conectado: " + socket.isConnected());
                    System.out.println("Socket bound: " + socket.isBound());
                }
            }
        } 
        catch (InterruptedException e) 
        {
            Thread.currentThread().interrupt();
        } 
        catch (IOException e) 
        {
            System.out.println("Excepcion: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene las acciones enviadas por el cliente a través de streamEntrada.
     * <dl>
     * <strong>Este método bloquea, porque invoca a streamEntrada.readLine()</strong>
     * </dl>
     * @param cliente El socket del cliente que envía la acción.
     * @param streamEntrada El stream de entrada del socket.
     * @return La acción enviada por el cliente, o null si la conexión fue interrumpida.
     * @throws IOException 
     */
    private AccionCliente obtenerAccion() throws IOException 
    {
        // Incializar los lectores para obtener los caracteres y líneas de 
        // la comunicación del cliente.
        InputStreamReader entradaSocket = new InputStreamReader(socket.getInputStream());
        BufferedReader streamEntrada = new BufferedReader(entradaSocket);
        
        String lineaEncabezado = streamEntrada.readLine();
        
        // Si readLinea() no retorna null, el socket del cliente sigue
        // conectado.
        if (lineaEncabezado != null)
        {
            // Obtener los detlles de la acción del cliente.
            AccionCliente accionCliente = AccionCliente.desdeEncabezado(lineaEncabezado);

            if (accionCliente.tieneCuerpo())
            {
                // Si la comunicación tiene un cuerpo, obtener los datos JSON
                // del streamEntrada.
                String cuerpo = streamEntrada.readLine();

                accionCliente.setCuerpoJSON(cuerpo);
            }
            
            return accionCliente;
        }
        
        return null;
    }

    /**
     * @brief Maneja una acción recibida del cliente y produce un EventoServidor.
     * 
     * @param accion La acción del cliente.
     * @throws IOException
     * @throws InterruptedException 
     */
    private void manejarAccion(AccionCliente accion) throws IOException, InterruptedException
    {
        try 
        {
            // Realizar la accion solicitada y producir un resultado.
            // NOTA: Algunos tipos de acciones tienen "efectos secundarios".
            EventoServidor resultado = realizarAccion(accion);

            // Enviar resultado al cliente.
            enviarEventoASocket(socket, resultado);
        }
        catch (IllegalArgumentException e) 
        {
            // Si la peticion tiene un error de formato detectado al intentar
            // hacer un parse de AccionCliente, regresar una respuesta de error 
            // al cliente.
            Evento eventoError = new Evento(TipoDeEvento.ERROR_CLIENTE, e.getMessage());

            queueEventos.offer(eventoError, BLOQUEO_MAX_EVT_MS, TimeUnit.MILLISECONDS);
            
            EventoServidor resultadoErr = new EventoServidor(
                TipoDeEvento.ERROR_CLIENTE, 
                idUsuario.get(),
                e.getMessage()
            );

            enviarEventoASocket(socket, resultadoErr);
        }
        catch (SocketTimeoutException e) 
        {
            System.out.println("Advertencia: la operacion de lectura hizo timeout.");
        }
        catch (IOException e) 
        {
            System.out.println(String.format(
                "Error de IO en los streams del socket: %s\n%s", 
                e.getMessage(),
                Arrays.toString(e.getStackTrace())
            ));
        }
    }

    private EventoServidor realizarAccion(AccionCliente accionCliente) throws InterruptedException 
    {
        ControladorChat controladorChat = new ControladorChat(estadoServidor, idUsuario);

        EventoServidor resultado = controladorChat.ejecutarAccion(socket, accionCliente);

        // Notificar con el evento resultante de la accion.
        Evento evento = new Evento(resultado.getTipoDeEvento(), resultado.getCuerpoJSON());
        queueEventos.offer(evento, BLOQUEO_MAX_EVT_MS, TimeUnit.MILLISECONDS);

        return resultado;
    }

    public static void enviarEventoASocket(Socket cliente, EventoServidor resultado) throws IOException 
    {
        OutputStreamWriter writerSalida = new OutputStreamWriter(cliente.getOutputStream());
        PrintWriter salida = new PrintWriter(writerSalida);

        System.out.println("Respuesta a enviar: " + resultado.comoRespuesta());
        
        synchronized (cliente) 
        {
            salida.println(resultado.comoRespuesta());
            salida.flush();
        }
    }
}
