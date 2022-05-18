package com.pasarceti.chat.servidor.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControladorBD {

    protected Connection c;
    
    public ControladorBD() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ControladorBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            c = DriverManager.getConnection(
                    "jdbc:mysql://localhost/chat","root","");
        } catch (SQLException ex) {
            Logger.getLogger(ControladorBD.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    public Connection getC() {
        return c;
    }

}
