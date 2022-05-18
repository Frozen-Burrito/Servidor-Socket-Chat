package com.pasarceti.chat.servidor.modelos.dto;

import com.pasarceti.chat.servidor.modelos.Invitacion;

/**
 * Una invitación realizada por un usuario, que invita a otro usuario a ser 
 * amigo del usuario que le invitó, o que invita a otro usuario a ser miembro 
 * de un grupo.
 * 
 */
public class DTOInvitacion 
{
    private int id;
    
    private final int idUsuarioQueInvita;
    
    private final int idUsuarioInvitado;

    private final Integer idGrupo;
    
    /**
     * Crea una nueva invitación de amistad.
     * @param idUsuarioQueInvita El usuario que hace la invitación.
     * @param idUsuarioInvitado El usuario invitado a ser amigo del usuario que invita.
     */
    public DTOInvitacion(int idUsuarioQueInvita, int idUsuarioInvitado) 
    {
        this.id = -1;
        this.idUsuarioQueInvita = idUsuarioQueInvita;
        this.idUsuarioInvitado = idUsuarioInvitado;
        this.idGrupo = null;
    }

    /**
     * Crea una nueva invitación a un grupo.
     * @param idUsuarioQueInvita El usuario que hace la invitación.
     * @param idUsuarioInvitado El usuario invitado a ser miembro del grupo.
     * @param idGrupo El grupo al que se invitó.
     */
    public DTOInvitacion(int idUsuarioQueInvita, int idUsuarioInvitado, Integer idGrupo) 
    {
        this.id = -1;
        this.idUsuarioQueInvita = idUsuarioQueInvita;
        this.idUsuarioInvitado = idUsuarioInvitado;
        this.idGrupo = idGrupo;
    }
    
    public static DTOInvitacion desdeModelo(Invitacion invitacion)
    {
        return new DTOInvitacion(
            invitacion.getId_usuario_emisor(),
            invitacion.getId_usuario_invitado(),
            invitacion.getId_grupo()
        );
    }
    
    /**
     * Determina si esta invitacion es de amistad.
     * @return true si es de amistad, false si es a un grupo.
     */
    public boolean esDeAmistad() 
    {
        return idGrupo == null;
    }

    public int getIdUsuarioQueInvita() 
    {
        return idUsuarioQueInvita;
    }

    public int getIdUsuarioInvitado() 
    {
        return idUsuarioInvitado;
    }
    
    public int getIdGrupo() {
        return idGrupo;
    }

    public int getId() 
    {
        return id;
    }

    public void setId(int id) 
    {
        this.id = id;
    }
}
