package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import com.pasarceti.chat.servidor.modelos.Comunicacion;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import java.util.Observable;

/**
 * Esta clase ejecuta la "tarea" de manejar la comunicacion con los sockets 
 * cliente.
 * 
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
            // Manejar la comunicación del socket.
            manejarPeticion(socket);

            socket.close();

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
    private void manejarPeticion(Socket cliente) throws IOException
    {
        // Incializar los lectores para obtener los caracteres y líneas de 
        // la comunicación del cliente.
        InputStreamReader inputReader = new InputStreamReader(cliente.getInputStream());
        BufferedReader lector = new BufferedReader(inputReader);

        try 
        {
            // Obtener líneas del lector.
            String primeraLinea = lector.readLine();
            String cuerpo = lector.readLine();

            // Obtener los detlles de la comunicación.
            Comunicacion peticion = Comunicacion.desdePeticion(primeraLinea, cuerpo);

            boolean errorDePeticion = peticion.getTipoDeEvento()!= TipoDeEvento.ERROR_CLIENTE.ordinal() && 
                                      peticion.getTipoDeEvento() != TipoDeEvento.ERROR_SERVIDOR.ordinal();

            if (errorDePeticion) 
            {
                // Si la peticion tiene un error detectado por
                // Comunicacion.desdePeticion(), regresar una respuesta de error 
                // al cliente.
                enviarRespuesta(cliente, peticion);
            }

            // Realizar la accion solicitada y producir un resultado.
            // NOTA: Algunos tipos de acciones tienen "efectos secundarios".
            Comunicacion resultado = realizarAccion(peticion);

            // Enviar resultado al cliente.
            enviarRespuesta(cliente, resultado);
        }
        catch (IOException e) 
        {
            lector.close();
            inputReader.close();
        }
    }

    private Comunicacion realizarAccion(Comunicacion comunicacion) 
    {
        //TODO: Implementar funciones del servidor.

        // Notificar con el evento resultante de la accion.
        Evento evento = new Evento(TipoDeEvento.USUARIO_CONECTADO, LocalDateTime.now(), "");
        notificarEvento(evento);

        // Serializar el objeto producido por la accion a JSON, usando GSON
        //TODO: Serializar el objeto de resultado real.
        Gson gson = new Gson();
        String cuerpoRespuesta = gson.toJson(evento);

        // La accion fue exitosa si el evento no es ningun tipo de error.
        boolean accionFueExitosa = evento.getTipoDeEvento() != TipoDeEvento.ERROR_CLIENTE && 
                                   evento.getTipoDeEvento() != TipoDeEvento.ERROR_SERVIDOR;

        return new Comunicacion(
            TipoDeEvento.USUARIO_CONECTADO.ordinal(), 
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

        salida.println(resultado.toString());
    }

    private void notificarEvento(Evento nuevoEvento) 
    {
        this.setChanged();
        this.notifyObservers(nuevoEvento);
        this.clearChanged();
    }
}
