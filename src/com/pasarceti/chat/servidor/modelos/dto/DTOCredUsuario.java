package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Un objeto de transferencia de datos para las credenciales de usuario.
 * 
 */
public class DTOCredUsuario 
{
    private transient final int idUsuario;

    private final String nombreUsuario;

    private final String password;

    public DTOCredUsuario(String nombreUsuario, String password) {
        this.idUsuario = -1; // -1, porque en teoria no le ha sido asignado un id.
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    public DTOCredUsuario(int idUsuario, String nombreUsuario, String password) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPassword() {
        return password;
    }
}
