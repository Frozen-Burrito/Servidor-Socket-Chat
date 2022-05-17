package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Representa un evento de abandono de grupo, contiene el grupo abandonado y el
 * ID del usuario que lo abandono.
 */
public class DTOAbandonarGrupo
{
    public final int idUsuarioQueAbandono;
    
    public final DTOGrupo grupoAbandonado;

    public DTOAbandonarGrupo(int idUsuarioQueAbandono, DTOGrupo grupoAbandonado)
    {
        this.idUsuarioQueAbandono = idUsuarioQueAbandono;
        this.grupoAbandonado = grupoAbandonado;
    }

    public int getIdUsuarioQueAbandono()
    {
        return idUsuarioQueAbandono;
    }

    public DTOGrupo getGrupoAbandonado()
    {
        return grupoAbandonado;
    }
}
