package com.pasarceti.chat.servidor.modelos.dto;

import com.pasarceti.chat.servidor.modelos.TipoDestinatario;

/**
 *
 * 
 */
public class DTOContacto 
{
    private final TipoDestinatario tipoDeContacto;

    private final int idContacto;

    public DTOContacto(int tipoDeContacto, int idContacto) 
    {    
        this.tipoDeContacto = TipoDestinatario.values()[tipoDeContacto];
        this.idContacto = idContacto;
    }
    
    public TipoDestinatario getTipoDestinatario() 
    {
        return tipoDeContacto;
    }

    public int getIdDestinatario() 
    {
        return idContacto;
    }
}
