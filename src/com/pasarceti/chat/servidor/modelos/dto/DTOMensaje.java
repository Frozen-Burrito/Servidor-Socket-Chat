package com.pasarceti.chat.servidor.modelos.dto;

import com.pasarceti.chat.servidor.modelos.TipoDestinatario;
import java.time.LocalDateTime;

/**
 *
 * 
 */
public class DTOMensaje 
{
    private int idMensaje;

    private final String contenido;

    private final LocalDateTime fecha;

    private final TipoDestinatario tipoDestinatario;

    private transient int idAutor;

    private final int idDestinatario;

    public DTOMensaje(int idMensaje, String contenido, int tipoDestinatario, int idDestinatario, int idAutor) {
        this.idMensaje = idMensaje;
        this.fecha = LocalDateTime.now();
        this.contenido = contenido;
        this.tipoDestinatario = TipoDestinatario.values()[tipoDestinatario];
        this.idDestinatario = idDestinatario;
        this.idAutor = idAutor;
    }

    public void setIdMensaje(int idMensaje) {
        this.idMensaje = idMensaje;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    public int getIdMensaje() {
        return idMensaje;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoDestinatario getTipoDestinatario() {
        return tipoDestinatario;
    }

    public int getIdAutor() {
        return idAutor;
    }

    public int getIdDestinatario() {
        return idDestinatario;
    }


}
