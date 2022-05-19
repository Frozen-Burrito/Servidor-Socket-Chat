package com.pasarceti.chat.servidor.modelos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrupoDAO {

    private final Connection conexionBD;
    
    public GrupoDAO(Connection conexionBD) {
        this.conexionBD = conexionBD; 
    }

    // Añadir Grupo
    public int crear(Grupo grupo) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("INSERT INTO grupo VALUES(?,?)");
            ps.setInt(1, grupo.getId());
            ps.setString(2, grupo.getNombre());
            ps.executeUpdate();
            
            ResultSet resultados = ps.getGeneratedKeys();
            
            if (resultados.next())
            {
                return resultados.getInt(1);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    // Buscar un grupo por Id
    public Grupo buscar_porId(int id) {
        PreparedStatement ps;
        ResultSet res;
        Grupo grupo = null;
        try {
            ps = conexionBD.prepareStatement("SELECT * from grupo WHERE id = ?");
            ps.setInt(1, id);
            res = ps.executeQuery();
            if (res.next()) {
                grupo = new Grupo();
                grupo.setId(res.getInt("id"));
                grupo.setNombre(res.getString("nombre"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return grupo;
    }

    // Buscar un grupo por su nombre
    public Grupo buscar_porNombre(String nombre) {
        PreparedStatement ps;
        ResultSet res;
        Grupo grupo = null;
        try {
            ps = conexionBD.prepareStatement("SELECT * from grupo WHERE nombre = ?");
            ps.setString(1, nombre);
            res = ps.executeQuery();
            if (res.next()) {
                grupo = new Grupo();
                grupo.setId(res.getInt("id"));
                grupo.setNombre(res.getString("nombre"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return grupo;
    }

    // Eliminar Grupo
    public void eliminar(Grupo grupo) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("DELETE FROM grupo WHERE id = ?");
            ps.setInt(1, grupo.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
