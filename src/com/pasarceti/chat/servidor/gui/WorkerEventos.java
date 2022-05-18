package com.pasarceti.chat.servidor.gui;

import com.pasarceti.chat.servidor.modelos.Evento;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;

/**
 * Consume los eventos producidos por el servidor y se los envía a la interfaz.
 */
public class WorkerEventos extends SwingWorker<Void, Evento>
{
    private final BlockingQueue<Evento> queueEventos;
    
    private final InterfazGrafica gui;

    public WorkerEventos(BlockingQueue<Evento> queueEventos, InterfazGrafica gui) {
        this.queueEventos = queueEventos;
        this.gui = gui;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        while (true) {
            Evento evento = queueEventos.poll(30, TimeUnit.SECONDS);
            
            if (evento != null)
            {
                publish(evento);
            }
        }
    }

    @Override
    protected void process(List<Evento> eventos) {
        gui.actualizarListaEventos(eventos.get(eventos.size() - 1));
    }
}
