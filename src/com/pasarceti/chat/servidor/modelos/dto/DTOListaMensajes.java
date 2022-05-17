package com.pasarceti.chat.servidor.modelos.dto;

import java.util.Collections;
import java.util.List;

/**
 * Una lista inicial de mensajes, usada para obtener los mensajes del usuario 
 * cuando no ha cargado sus conversaciones anteriores.
 */
public class DTOListaMensajes
{
    private final List<DTOMensaje> mensajes;
    
    public DTOListaMensajes(List<DTOMensaje> mensajes)
    {        
        // Asegurar que este objeto solo contenga hasta 100 mensajes a la vez.
        this.mensajes = (mensajes.size() > 100) ? mensajes.subList(0, 100) : mensajes;
    }

    public List<DTOMensaje> getMensajes()
    {
        return Collections.unmodifiableList(mensajes);
    }
}
