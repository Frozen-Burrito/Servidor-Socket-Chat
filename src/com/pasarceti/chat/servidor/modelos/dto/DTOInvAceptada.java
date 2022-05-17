package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Permite al cliente saber que aceptó la invitación con éxito, además de 
 * que contiene los datos del nuevo amigo del usuario.
 */
public class DTOInvAceptada 
{
    private final DTOInvitacion invitacion;
    
    private final DTOUsuario usuarioQueAcepto;
    
    public DTOInvAceptada(DTOInvitacion invitacion, DTOUsuario usuarioQueAcepto)
    {
        this.invitacion = invitacion;
        this.usuarioQueAcepto = usuarioQueAcepto;
    }

    public DTOInvitacion getInvitacion() 
    {
        return invitacion;
    }

    public DTOUsuario getUsuarioQueAcepto() 
    {
        return usuarioQueAcepto;
    }
}
