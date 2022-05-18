package com.pasarceti.chat.servidor.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControladorBD {

    protected Connection c;
    
    private static CredencialesBD credenciales = null;
    
    public ControladorBD() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ControladorBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            c = DriverManager.getConnection(
                    credenciales.getUrl(), 
                    credenciales.getUsuario(), 
                    credenciales.getPassword()
            );
        } catch (SQLException ex) {
            Logger.getLogger(ControladorBD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(ControladorBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void setCredenciales(CredencialesBD nuevasCredenciales) {
        // Cuidado, esto no es una buena manera de hacerlo.
        credenciales = nuevasCredenciales;
    }

    public Connection getC() {
        return c;
    }
}
