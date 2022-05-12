package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import com.pasarceti.chat.servidor.modelos.AccionCliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.pasarceti.chat.servidor.modelos.Comunicacion;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Esta clase ejecuta la "tarea" de manejar la comunicacion con los sockets 
 * cliente.
 */
public class HiloServidor implements Runnable, PropertyChangeListener 
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

    // El queue para el patrón productor-consumidor (servidor y gui, en este caso)
    // que envía los eventos producidos por el servidor a los consumidores de este queue.
    private final BlockingQueue<Evento> queueEventos;

    public HiloServidor(Socket socket, EstadoServidor estadoServidor, BlockingQueue<Evento> queueEventos) 
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
            // Agregar este socket como listener de eventos de usuarios conectados.
            estadoServidor.agregarListener(EstadoServidor.PROP_USUARIOS_CONECTADOS, this);

            // Incializar los lectores para obtener los caracteres y líneas de 
            // la comunicación del cliente.
            InputStreamReader entradaSocket = new InputStreamReader(socket.getInputStream());
            BufferedReader streamEntrada = new BufferedReader(entradaSocket);

            while (!(socket.isClosed()))
            {
                String lineaEncabezado = streamEntrada.readLine();

                // Si readLinea() no retorna null, el socket del cliente sigue
                // conectado.
                if (lineaEncabezado != null)
                {
                    manejarPeticion(socket, lineaEncabezado, streamEntrada);
                }
                else 
                {
                    // Si la linea del encabezado fue null, el socket fue desconectado.
                    // Hacer cleanup necesario.
                    System.out.println("El cliente se desconectó, cerrando el socket.");

                    estadoServidor.desconectarUsuarioActual();
                    
                    socket.close();
                    streamEntrada.close();

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
        finally 
        {
            estadoServidor.removerListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        final Gson gson = new Gson();
        
        EventoServidor actualizacion;

        switch (evt.getPropertyName())
        {
        case EstadoServidor.PROP_USUARIOS_CONECTADOS:
            final ConcurrentHashMap<Integer, Socket> usuariosConectados = estadoServidor.getClientesConectados();
            final String usuariosConectadosStr = gson.toJson(usuariosConectados);

            actualizacion = new EventoServidor(
                TipoDeEvento.USUARIO_CONECTADO, 
                estadoServidor.getIdUsuarioActual(), 
                usuariosConectadosStr
            );
            break;
        case EstadoServidor.PROP_MENSAJES_RECIBIDOS:
            final List<DTOMensaje> mensajes = estadoServidor.getMensajesUsuarioActual();
            final String jsonMensajesRecibidos = gson.toJson(mensajes);

            actualizacion = new EventoServidor(
                TipoDeEvento.MENSAJE_ENVIADO, 
                estadoServidor.getIdUsuarioActual(), 
                jsonMensajesRecibidos
            );
            break;
        default:
            actualizacion = new EventoServidor(
                TipoDeEvento.ERROR_SERVIDOR, 
                estadoServidor.getIdUsuarioActual(),
                ""
            );
        }

        try 
        {
            enviarRespuesta(socket, actualizacion);
        }      
        catch (IOException e)  
        {
            Evento eventoErr = new Evento(TipoDeEvento.ERROR_SERVIDOR, e.getMessage());
            queueEventos.offer(eventoErr);
        }
    }

   /**
     * @brief Realiza todas las acciones necesarias para procesar una peticion.

     * - Interpreta la peticion.
     * - Ejecuta la accion solicitada.
     * - Retorna un resultado al cliente.
    */
    private void manejarPeticion(Socket cliente, String lineaEncabezado, BufferedReader streamEntrada) throws IOException, InterruptedException
    {
        try 
        {       
            // Obtener los detlles de la acción del cliente.
            AccionCliente accionCliente = AccionCliente.desdeEncabezado(lineaEncabezado);

            if (accionCliente.tieneJson())
            {
                // Si la comunicación tiene un cuerpo, obtener los datos JSON
                // del streamEntrada.
                String cuerpo = streamEntrada.readLine();

                accionCliente.setCuerpoJSON(cuerpo);
            }

            // Realizar la accion solicitada y producir un resultado.
            // NOTA: Algunos tipos de acciones tienen "efectos secundarios".
            EventoServidor resultado = realizarAccion(accionCliente);

            // Enviar resultado al cliente.
            enviarRespuesta(cliente, resultado);
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
                estadoServidor.getIdUsuarioActual(),
                e.getMessage()
            );

            enviarRespuesta(cliente, resultadoErr);
        }
        catch (SocketTimeoutException e) 
        {
            System.out.println("Advertencia: la operacion de lectura hizo timeout.");
        }
        catch (IOException e) 
        {
            System.out.println("Error de IO en los streams del socket: " + e.getMessage() + e.getStackTrace());
        }
    }

    private EventoServidor realizarAccion(AccionCliente accionCliente) throws InterruptedException 
    {
        //TODO: Implementar todas las funciones del servidor.
        ControladorChat controladorChat = new ControladorChat(estadoServidor);

        EventoServidor resultado = controladorChat.ejecutarAccion(accionCliente);

        // Notificar con el evento resultante de la accion.
        Evento evento = new Evento(resultado.getTipoDeEvento(), resultado.getCuerpoJSON());
        queueEventos.offer(evento, BLOQUEO_MAX_EVT_MS, TimeUnit.MILLISECONDS);

        return resultado;
    }

    private void enviarRespuesta(Socket cliente, EventoServidor resultado) throws IOException 
    {
        OutputStreamWriter writerSalida = new OutputStreamWriter(cliente.getOutputStream());
        PrintWriter salida = new PrintWriter(writerSalida);

        System.out.println("Respuesta a enviar: " + resultado.comoRespuesta());

        salida.println(resultado.comoRespuesta());

        salida.flush();
    }
}
