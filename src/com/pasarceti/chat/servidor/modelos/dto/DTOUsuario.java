package com.pasarceti.chat.servidor.modelos.dto;

/**
 *
 * 
 */
public class DTOUsuario extends DTODestinatario
{
    private final int idUsuario;

    private final String nombreUsuario;

    public DTOUsuario(int idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}
