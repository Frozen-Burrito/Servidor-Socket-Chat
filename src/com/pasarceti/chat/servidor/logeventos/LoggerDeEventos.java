/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.logeventos;

import com.pasarceti.chat.servidor.modelos.Evento;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Es un consumidor de logs de eventos. Continuamente recibe logs desde una fila
 * que bloquea (BlockingQueue), y los muestra usando un Logger. 
 * 
 */
public class LoggerDeEventos implements Runnable 
{
    private static final Logger logger = Logger.getLogger(LoggerDeEventos.class.toString());

    // La fila que recibe los eventos producidos por otros hilos.
    private final BlockingQueue<Evento> queueEventos;

    // La lista con los eventos consumidos por este objeto.
    // NOTA: Es posible bloquear a LoggerDeEventos si se itera la lista de
    // eventos conumidos y se hace un proceso tardado por cada elemento. No hacer esto.
    private final List<Evento> eventosConsumidos = Collections.synchronizedList(new ArrayList<>());

    public LoggerDeEventos(BlockingQueue queue)
    {
        this.queueEventos = queue;
    }

    @Override
    public void run() 
    {
        while (true)
        {
            try 
            {
                Evento evento = queueEventos.poll(1, TimeUnit.MINUTES);

                if (evento != null) 
                {
                    consumirEvento(evento);
                }
            }
            catch (InterruptedException e) 
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void start()
    {
        Thread tLogEventos = new Thread(this);
        tLogEventos.start();
    }

    private void consumirEvento(Evento evento)
    {
        logger.info(evento.toString());

        eventosConsumidos.add(evento);
    }

    public List<Evento> getEventosConsumidos() {
        return eventosConsumidos;
    }
}
