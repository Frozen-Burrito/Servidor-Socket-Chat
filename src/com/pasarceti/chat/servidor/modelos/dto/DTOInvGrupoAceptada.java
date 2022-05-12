package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Permite al cliente saber que aceptó la invitación con éxito, además de 
 * que contiene los datos del nuevo grupo en el que esta el usuario.
 */
public class DTOInvGrupoAceptada 
{
    private final int id;
    
    private final DTOGrupo nuevoGrupo;
    
    public DTOInvGrupoAceptada(int id, DTOGrupo nuevoGrupo)
    {
        this.id = id;
        this.nuevoGrupo = nuevoGrupo;
    }

    public int getId() 
    {
        return id;
    }

    public DTOGrupo getNuevoAmigo() 
    {
        return nuevoGrupo;
    }
}
