package com.pasarceti.chat.servidor.modelos;

import java.net.Socket;

/**
 * Representa un cliente conectado al sistema, incluyendo su socket de conexión.
 */
public class Cliente
{
    private final Integer id;
    
    private final Socket socket;
    
    public Cliente(int id, Socket socket)
    {
        this.id = id;
        this.socket = socket;
    }

    public Integer getId()
    {
        return id;
    }

    public Socket getSocket()
    {
        return socket;
    }
}
