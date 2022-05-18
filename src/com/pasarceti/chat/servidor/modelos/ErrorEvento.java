package com.pasarceti.chat.servidor.modelos;

/**
 *
 */
public class ErrorEvento
{
    private final String mensaje;

    public ErrorEvento(String mensaje)
    {
        this.mensaje = mensaje;
    }

    public String getMensaje()
    {
        return mensaje;
    }
}
