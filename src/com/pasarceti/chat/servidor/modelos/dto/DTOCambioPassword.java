package com.pasarceti.chat.servidor.modelos.dto;

/**
 *
 * 
 */
public class DTOCambioPassword 
{
    private int idUsuario;

    private String nuevaPassword;

    public DTOCambioPassword(int idUsuario, String nuevaPassword) 
    {
        this.idUsuario = idUsuario;
        this.nuevaPassword = nuevaPassword;
    }

    public int getIdUsuario() 
    {
        return idUsuario;
    }

    public String getNuevaPassword() 
    {
        return nuevaPassword;
    }
}
