package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Un objeto que transporta el ID de una entidad, usualmente para transformarlo
 * en un objeto JSON.
 * 
 */
public class DTOIdEntidad
{
    private final int id;

    public DTOIdEntidad(int id) 
    {
        this.id = id;
    }

    public int getId() 
    {
        return id;
    }
}
