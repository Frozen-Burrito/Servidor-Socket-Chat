package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Observable;

import com.pasarceti.chat.servidor.modelos.Comunicacion;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;

/**
 * Esta clase ejecuta la "tarea" de manejar la comunicacion con los sockets 
 * cliente.
 */
public class HiloServidor extends Observable implements Runnable 
{
    // El socket con la conexión del cliente.
    private final Socket socket;

    public HiloServidor(Socket socket) 
    {
        this.socket = socket;
    }

    @Override
    public void run() 
    {
        try 
        {
            // Incializar los lectores para obtener los caracteres y líneas de 
            // la comunicación del cliente.
            InputStreamReader entradaSocket = new InputStreamReader(socket.getInputStream());
            BufferedReader streamEntrada = new BufferedReader(entradaSocket);

            while (!(socket.isClosed()))
            {
                String primeraLinea = streamEntrada.readLine();
                System.out.println("Encabezado: " + primeraLinea);

                // Si readLinea() retorna null, el socket del cliente fue desconectado.
                if (primeraLinea == null)
                {
                    System.out.println("El cliente se desconectó, cerrando el socket.");
                    socket.close();

                    System.out.println("Socket cerrado: " + socket.isClosed());
                    System.out.println("Socket conectado: " + socket.isConnected());
                    System.out.println("Socket bound: " + socket.isBound());
                }
                else 
                {
                    // Obtener los detlles de la comunicación.
                    Comunicacion peticion = Comunicacion.desdePeticion(primeraLinea);

                    // Manejar la comunicación con el socket cliente.
                    manejarPeticion(socket, peticion, streamEntrada);
                }
            }

            streamEntrada.close();

        } catch (IOException e) 
        {
            System.out.println("Excepcion: " + e.getMessage());
        }
    }

   /**
     * @brief Realiza todas las acciones necesarias para procesar una peticion.

     * - Interpreta la peticion.
     * - Ejecuta la accion solicitada.
     * - Retorna un resultado al cliente.
    */
    private void manejarPeticion(Socket cliente, Comunicacion comunicacion, BufferedReader streamEntrada) throws IOException
    {
        try 
        {            
            if (!comunicacion.tieneError() && comunicacion.tieneJson())
            {
                // Si la comunicación tiene un cuerpo, obtener los datos JSON
                // del streamEntrada.
                String cuerpo = streamEntrada.readLine();

                comunicacion.setCuerpoJSON(cuerpo);
            }

            if (comunicacion.tieneError()) 
            {
                System.out.println("Error de peticion");
                // Si la peticion tiene un error detectado por
                // Comunicacion.desdePeticion(), regresar una respuesta de error 
                // al cliente.
                enviarRespuesta(cliente, comunicacion);
            } 
            else 
            {
                // Realizar la accion solicitada y producir un resultado.
                // NOTA: Algunos tipos de acciones tienen "efectos secundarios".
                Comunicacion resultado = realizarAccion(comunicacion);

                // Enviar resultado al cliente.
                enviarRespuesta(cliente, resultado);
            }
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

    private Comunicacion realizarAccion(Comunicacion comunicacion) 
    {
        //TODO: Implementar todas las funciones del servidor.
        int ordTipoDeAccion = Math.min(comunicacion.getTipoDeEvento(), TipoDeEvento.values().length);
        TipoDeEvento accionSolicitada = TipoDeEvento.values()[ordTipoDeAccion];

        Evento evento;

        final int idUsuario = comunicacion.getIdUsuarioCliente();
        final String datosJson = comunicacion.getCuerpoJSON();

        switch (accionSolicitada)
        {
        case USUARIO_REGISTRADO:
            evento = AccionesServidor.registrarUsuario(datosJson);
            break;
        case USUARIO_CONECTADO:
            evento = AccionesServidor.accederUsuario(datosJson);
            break;
        case USUARIO_DESCONECTADO:
            evento = AccionesServidor.desconectarUsuario(idUsuario);
            break;
        default:
            evento = new Evento(TipoDeEvento.ERROR_SERVIDOR, "La acción no pudo ser procesada por el servidor.");
            break;
        }

        System.out.println(evento.toString());

        // Notificar con el evento resultante de la accion.
        notificarEvento(evento);

        // Serializar el objeto producido por la accion a JSON, usando GSON
        //TODO: Serializar el objeto de resultado real.
        Gson gson = new Gson();
        String cuerpoRespuesta = gson.toJson(evento.getDatos());

        // La accion fue exitosa si el evento no es ningun tipo de error.
        boolean accionFueExitosa = evento.getTipoDeEvento() != TipoDeEvento.ERROR_CLIENTE && 
                                   evento.getTipoDeEvento() != TipoDeEvento.ERROR_SERVIDOR;

        return new Comunicacion(
            evento.getTipoDeEvento().ordinal(), 
            accionFueExitosa, 
            cuerpoRespuesta.length(), 
            comunicacion.getIdUsuarioCliente(),
            cuerpoRespuesta
        );
    }

    private void enviarRespuesta(Socket cliente, Comunicacion resultado) throws IOException 
    {
        OutputStreamWriter writerSalida = new OutputStreamWriter(cliente.getOutputStream());
        PrintWriter salida = new PrintWriter(writerSalida);

        System.out.println("Respuesta a enviar: " + resultado.toString());

        salida.println(resultado.toString());

        salida.flush();
    }

    private void notificarEvento(Evento nuevoEvento) 
    {
        this.setChanged();
        this.notifyObservers(nuevoEvento);
        this.clearChanged();
    }
}
