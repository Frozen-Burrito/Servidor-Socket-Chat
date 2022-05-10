package com.pasarceti.chat.servidor.controladores;

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

/**
 *
 * 
 */
public class HiloServidor implements Runnable 
{

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
            String lineaInicial = lector.readLine();
            String cuerpo = lector.readLine();

            // Obtener los detlles de la comunicación.
            Comunicacion comunicacion = InterpreteDeComunicacion.interpretarPeticion(lineaInicial + cuerpo);

            Evento eventoResultante = realizarAccion(comunicacion);

            enviarRespuesta(cliente, comunicacion);
        }
        catch (IOException e) 
        {
            lector.close();
            inputReader.close();
        }
    }

    private void enviarRespuesta(Socket cliente, Comunicacion comunicacion) throws IOException 
    {
        OutputStreamWriter writerSalida = new OutputStreamWriter(cliente.getOutputStream());
        PrintWriter salida = new PrintWriter(writerSalida);

        String strRespuesta = InterpreteDeComunicacion.formarRespuesta(comunicacion);

        salida.println(strRespuesta);
    }

    private Evento realizarAccion(Comunicacion comunicacion) 
    {
        return new Evento(TipoDeEvento.USUARIO_CONECTADO, LocalDateTime.now(), "");
    }

    private void enviarEvento(Evento evento) 
    {

    }
}
