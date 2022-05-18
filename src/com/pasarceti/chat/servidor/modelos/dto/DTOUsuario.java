package com.pasarceti.chat.servidor.modelos.dto;

import com.pasarceti.chat.servidor.modelos.Usuario;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contiene los datos publicos de un usuario, sin incluir su contraseña.
 * 
 */
public class DTOUsuario 
{
    private final int idUsuario;

    private final String nombreUsuario;
    
    // Es un atributo de presentación, usado para facilitar 
    private final AtomicBoolean conectado = new AtomicBoolean(false);
    
    public DTOUsuario(int idUsuario, String nombreUsuario) 
    {
        this(idUsuario, nombreUsuario, false);
    }

    public DTOUsuario(int idUsuario, String nombreUsuario, boolean conectado) 
    {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        
        this.conectado.set(conectado);
    }
    
    public static DTOUsuario desdeModelo(Usuario usuario)
    {
        return new DTOUsuario(
            usuario.getId(),
            usuario.getNombre_usuario(),
            false
        );
    }

    public int getIdUsuario() 
    {
        return idUsuario;
    }

    public String getNombreUsuario() 
    {
        return nombreUsuario;
    }

    public boolean getConectado()
    {
        return conectado.get();
    }
    
    public void setConectado(boolean conectado)
    {
        this.conectado.set(conectado);
    }
}
