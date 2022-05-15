package com.pasarceti.chat.servidor.modelos;

import java.time.LocalDateTime;

/**
 * Un evento del servidor chat, es enviado por cada accion de un cliente.
 */
public class Evento 
{
    private final TipoDeEvento tipo;

    private final LocalDateTime fecha;

    private final String datos;

    public Evento(TipoDeEvento tipo, String datos) 
    {
        this.tipo = tipo;
        this.fecha = LocalDateTime.now();
        this.datos = datos;
    }

    public Evento(TipoDeEvento tipo, LocalDateTime fecha, String datos) 
    {
        this.tipo = tipo;
        this.fecha = fecha;
        this.datos = datos;
    }

    @Override
    public String toString() 
    {
        return String.format("[%s] - %s: %s", fecha.toString(), tipo.toString(), datos);
    }

    public LocalDateTime getFecha() 
    {
        return fecha;
    }

    public String getDatos() 
    {
        return datos;
    }

    public final TipoDeEvento getTipoDeEvento() 
    {
        return tipo;
    }
}
