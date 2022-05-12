package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Permite al cliente saber que aceptó la invitación con éxito, además de 
 * que contiene los datos del nuevo amigo del usuario.
 */
public class DTOInvAmigoAceptada 
{
    private final int id;
    
    private final DTOUsuario nuevoAmigo;
    
    public DTOInvAmigoAceptada(int id, DTOUsuario nuevoAmigo)
    {
        this.id = id;
        this.nuevoAmigo = nuevoAmigo;
    }

    public int getId() 
    {
        return id;
    }

    public DTOUsuario getNuevoAmigo() 
    {
        return nuevoAmigo;
    }
}
