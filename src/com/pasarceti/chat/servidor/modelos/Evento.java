package com.pasarceti.chat.servidor.modelos;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Un evento del servidor chat, es enviado por cada accion de un cliente.
 */
public class Evento {
    
    private static final AtomicInteger generadorIds = new AtomicInteger();
    
    private final int id; 
    private final TipoDeEvento tipo;
    private final TipoDeAccion accion;
    private final LocalDateTime fecha;
    private final String descripcionOpcional;
    private final EventoServidor eventoProducido;

    public Evento(TipoDeEvento tipo, EventoServidor eventoProducido) {
        this(tipo, eventoProducido, "");
    }
  
    public Evento(TipoDeAccion accionManejada, EventoServidor eventoProducido) {
        this(accionManejada, eventoProducido, "");
    }
    
    public Evento(TipoDeEvento tipo, EventoServidor eventoProducido, String descripcionOpcional) {
        this.id = generadorIds.incrementAndGet();
        this.tipo = tipo;
        this.accion = null;
        this.eventoProducido = eventoProducido;
        this.fecha = LocalDateTime.now();
        this.descripcionOpcional = descripcionOpcional;
    }
    
    public Evento(TipoDeAccion accion, EventoServidor eventoProducido, String descripcionOpcional) {
        this.id = generadorIds.incrementAndGet();
        this.tipo = null;
        this.accion = accion;
        this.eventoProducido = eventoProducido;
        this.fecha = LocalDateTime.now();
        this.descripcionOpcional = descripcionOpcional;
    }
  
    @Override
    public String toString() 
    {
        return String.format(
                "[%s] - %s%s%s", 
                fecha.toString(), 
                tipo, 
                ": " + descripcionOpcional,
                (eventoProducido.esRespuesta()) ? ", resultado: " + eventoProducido.getTipoDeEvento() : ""
        );
    }

    public int getId() 
    {
        return id;
    }
  
    public final TipoDeEvento getTipoDeEvento() 
    {
        return tipo;
    }

    public TipoDeAccion getAccion()
    {
        return accion;
    }

    public LocalDateTime getFecha() 
    {
        return fecha;
    }

    public EventoServidor getEventoProducido()
    {
        return eventoProducido;
    }

    public String getDescripcion() 
    {
        return descripcionOpcional;
    }
}
