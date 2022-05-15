package com.pasarceti.chat.servidor.modelos;

import com.pasarceti.chat.servidor.bd.ControladorBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrupoDAO extends ControladorBD {

    public GrupoDAO() {
        super();
    }

    // Añadir Grupo
    public void crear(Grupo grupo) {
        PreparedStatement ps;
        try {
            ps = getC().prepareStatement("INSERT INTO grupo VALUES(?,?)");
            ps.setInt(1, grupo.getId());
            ps.setString(2, grupo.getNombre());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Buscar un grupo por Id
    public Grupo buscar_porId(int id) {
        PreparedStatement ps;
        ResultSet res;
        Grupo grupo = new Grupo();
        try {
            ps = getC().prepareStatement("SELECT * from grupo WHERE id = ?");
            ps.setInt(1, id);
            res = ps.executeQuery();
            if (res.next()) {
                grupo.setId(res.getInt("id"));
                grupo.setNombre(res.getString("nombre"));
            }
        } catch (SQLException ex) {
            grupo = null;
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return grupo;
    }

    // Buscar un grupo por su nombre
    public Grupo buscar_porNombre(String nombre) {
        PreparedStatement ps;
        ResultSet res;
        Grupo grupo = new Grupo();
        try {
            ps = getC().prepareStatement("SELECT * from grupo WHERE nombre = ?");
            ps.setString(1, nombre);
            res = ps.executeQuery();
            if (res.next()) {
                grupo.setId(res.getInt("id"));
                grupo.setNombre(res.getString("nombre"));
            }
        } catch (SQLException ex) {
            grupo = null;
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return grupo;
    }

    // Eliminar Grupo
    public void eliminar(Grupo grupo) {
        PreparedStatement ps;
        try {
            ps = getC().prepareStatement("DELETE FROM grupo WHERE id = ?");
            ps.setInt(1, grupo.getId());
            ps.executeUpdate();
        } catch (Exception ex) {
            Logger.getLogger(GrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
