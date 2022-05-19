package com.pasarceti.chat.servidor.bd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Almacena las credenciales de conexión a una BD, puede obtenerlas desde un 
 * archivo de configuración.
 */
public class CredencialesBD
{
    private final String url;
    private final String usuario;
    private final String password;
    
    public static final String CONFIG_URL = "url";
    public static final String CONFIG_USUARIO = "usuario";
    public static final String CONFIG_PASSWORD = "password";

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
    
    public static CredencialesBD desdeArchivoConfig(String nombreArchivo) throws FileNotFoundException, IOException
    {
        File archivoConfig = new File(nombreArchivo);
        
        if (!archivoConfig.exists())
        {
            archivoConfig.createNewFile();
        }

        Scanner lectorConfig = new Scanner(archivoConfig);
        
        // Parametros de conexion por defecto.
        String urlConfig = "jdbc:mysql://localhost/chat";
        String usuarioConfig = "root";
        String passwordConfig = "";
        
        while (lectorConfig.hasNextLine())
        {
            String linea = lectorConfig.nextLine();
            
            String[] entradaConfig = linea.split(": ", 2);
                    
            if (entradaConfig.length != 2)
            {
                throw new IllegalArgumentException("El archivo de configuración tiene una línea inválida");
            }
            
            String llave = entradaConfig[0].trim();
            String valor = entradaConfig[1].trim();
            valor = valor.substring(1, valor.length() - 1);
                        
            switch (llave)
            {
                case CONFIG_URL:
                    urlConfig = valor;
                    break;
                case CONFIG_USUARIO:
                    usuarioConfig = valor;
                    break;
                case CONFIG_PASSWORD:
                    passwordConfig = valor;
                    break;
            }
            
        }
        
        return new CredencialesBD(
            urlConfig,
            usuarioConfig,
            passwordConfig
        );
    }
}
