package com.pasarceti.chat.servidor.modelos.dto;

/**
 *
 * 
 */
public class DTOModInvitacion 
{
    private final int idInvitacion;

    public DTOModInvitacion(int idInvitacion) 
    {
        this.idInvitacion = idInvitacion;
    }

    public int getIdInvitacion() 
    {
        return idInvitacion;
    }

    public String toString() 
    {
        return "Invitación aceptada/rechazada: " + String.valueOf(idInvitacion);
    }
}
