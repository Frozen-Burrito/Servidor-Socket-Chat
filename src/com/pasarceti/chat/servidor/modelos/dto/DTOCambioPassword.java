package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Representa una solicitud de recuperación de contraseña hecha por un cliente.
 * 
 */
public class DTOCambioPassword 
{
    private final int idUsuario;

    private final String nuevaPassword;

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
