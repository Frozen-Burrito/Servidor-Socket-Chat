package com.pasarceti.chat.servidor.bd;

/**
 *
 */
public class CredencialesBD
{
    private final String url;
    private final String usuario;
    private final String password;

    public CredencialesBD(String url, String usuario, String password)
    {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUsuario()
    {
        return usuario;
    }

    public String getPassword()
    {
        return password;
    }
}
