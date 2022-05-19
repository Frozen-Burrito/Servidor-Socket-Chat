package com.pasarceti.chat.servidor.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Mantiene una pool de conexiones a una base de datos.
 */
public class PoolConexionesBD
{
    private final CredencialesBD credenciales;
    
    private static final int CAPACIDAD_INICIAL = 50;
    
    LinkedList<Connection> conexiones = new LinkedList<>();
    
    public PoolConexionesBD(CredencialesBD credenciales)
    {
        this.credenciales = credenciales;
        
        try 
        {
            iniciarPool();
        }
        catch (SQLException e)
        {
            System.out.println("Error inicializando pool de conexiones a BD: " + e.getMessage());
        } 
        catch (ClassNotFoundException e) 
        {
            System.out.println("Clase driver para MySQL no encontrada: " + e.getMessage());

        }
    }
    
    private void iniciarPool() throws SQLException, ClassNotFoundException
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        for (int i = 0; i < CAPACIDAD_INICIAL; ++i)
        {
            Connection conexion = DriverManager.getConnection(
                credenciales.getUrl(), 
                credenciales.getUsuario(), 
                credenciales.getPassword()
            );
            
            conexiones.add(conexion);
        }
    }
    
    public synchronized Connection getConexion() throws SQLException
    {
        if (conexiones.isEmpty())
        {
             Connection conexion = DriverManager.getConnection(
                credenciales.getUrl(), 
                credenciales.getUsuario(), 
                credenciales.getPassword()
            );
            
            conexiones.add(conexion);
        }
        
        return conexiones.pop();
    }
    
    public synchronized void regresarConexion(Connection conexion)
    {
        conexiones.push(conexion);
    }
}
