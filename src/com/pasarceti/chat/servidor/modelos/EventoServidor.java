/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos;

/**
 * Un evento que involucró la modificación de los datos del servidor.
 * Puede ser un resultado de una acción o  puede ser producido como efecto
 * secundario de otra acción.
 * 
 */
public class EventoServidor extends Comunicacion {

    private TipoDeEvento tipoDeEvento;
    
    public EventoServidor(TipoDeEvento tipoDeEvento, int idUsuarioCliente, String cuerpoJSON) 
    {
        // Una solicitud de acción es exitosa por defecto.
        super(esError(tipoDeEvento), cuerpoJSON.length(), idUsuarioCliente, cuerpoJSON);

        // Especificar el tipo de acción del cliente.
        this.tipoDeEvento = tipoDeEvento;
    }
    
    public EventoServidor(TipoDeEvento tipoDeEvento, String cuerpoJSON) 
    {
        this(tipoDeEvento, -1, cuerpoJSON);
    }

    public String comoRespuesta() 
    {
        String lineaEncabezado = String.format(
            "%s %d %s %d %d", 
            IDENTIFICADOR_COM, 
            tipoDeEvento.ordinal(), 
            String.valueOf(fueExitosa), 
            longitudCuerpo, 
            idUsuarioCliente
        );

        return String.format("%s\n%s", lineaEncabezado, cuerpoJSON);
    }

    private static boolean esError(TipoDeEvento evento) 
    {
        return evento == TipoDeEvento.ERROR_CLIENTE ||
            evento == TipoDeEvento.ERROR_SERVIDOR ||
            evento == TipoDeEvento.ERROR_AUTENTICACION;
    }

    public boolean tieneError() 
    {
        return tipoDeEvento == TipoDeEvento.ERROR_CLIENTE ||
               tipoDeEvento == TipoDeEvento.ERROR_SERVIDOR ||
               tipoDeEvento == TipoDeEvento.ERROR_AUTENTICACION;
    }

    public TipoDeEvento getTipoDeEvento() 
    {
        return tipoDeEvento;
    }
    
    public void setTipoDeEvento(TipoDeEvento tipoDeEvento) 
    {
        this.tipoDeEvento = tipoDeEvento;
    }
}
