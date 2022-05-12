package com.pasarceti.chat.servidor.modelos.dto;

import com.pasarceti.chat.servidor.modelos.TipoDestinatario;

/**
 *
 * 
 */
public class DTONuevoMensaje 
{
    private final String contenido;

    private final TipoDestinatario tipoDestinatario;

    private final int idDestinatario;

    public DTONuevoMensaje(String contenido, int tipoDestinatario, int idDestinatario) {
        this.contenido = contenido;
        this.tipoDestinatario = TipoDestinatario.values()[tipoDestinatario];
        this.idDestinatario = idDestinatario;
    }
    
    public String getContenido() {
        return contenido;
    }

    public TipoDestinatario getTipoDestinatario() {
        return tipoDestinatario;
    }

    public int getIdDestinatario() {
        return idDestinatario;
    }
}
