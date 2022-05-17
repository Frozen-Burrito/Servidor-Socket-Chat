package com.pasarceti.chat.servidor.modelos;

import com.pasarceti.chat.servidor.bd.ControladorBD;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AmistadDAO extends ControladorBD {

    public AmistadDAO() {
        super();
    }

    // Añadir amistad
    public void crear(Amistad amistad) {
        PreparedStatement ps;
        try {
            ps = getC().prepareStatement("INSERT INTO amistad VALUES(?,?)");
            ps.setInt(1, amistad.getId_usuario());
            ps.setInt(2, amistad.getId_otro_usuario());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(AmistadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Buscar una amistad ya existente
    public Amistad leer(int id_usuario, int id_otro_usuario) {
        PreparedStatement ps;
        ResultSet res;
        Amistad amistad = null;
        try {
            ps = getC().prepareStatement("SELECT * from amistad WHERE id_usuario = ? AND id_otro_usuario = ?");
            ps.setInt(1, id_usuario);
            ps.setInt(2, id_otro_usuario);
            res = ps.executeQuery();
            if (res.next()) {
                amistad.setId_usuario(res.getInt("id_usuario"));
                amistad.setId_otro_usuario(res.getInt("id_otro_usuario"));
            }
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(AmistadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return amistad;
    }

    // Buscar una lista de amistades de un dado usuario
    public List<Amistad> busqueda_porUsuario(int id) {
        PreparedStatement ps;
        ResultSet res;
        List<Amistad> amistades = new ArrayList<>();
        try {
            ps = getC().prepareStatement("SELECT * from amistad WHERE id_usuario = ?");
//            ps.setInt(1, usuario.getId());
            ps.setInt(1, id);
            res = ps.executeQuery();
            while (res.next()) {
                Amistad amistad = new Amistad();
                amistad.setId_usuario(res.getInt("id_usuario"));
                amistad.setId_otro_usuario(res.getInt("id_otro_usuario"));
                amistades.add(amistad);
            }
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(AmistadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return amistades;
    }

    // Eliminar amistad
    public void eliminar(Amistad amistad) {
        PreparedStatement ps;
        try {
            ps = getC().prepareStatement("DELETE FROM amistad WHERE id_usuario = ? AND id_otro_usuario = ?");
            ps.setInt(1, amistad.getId_usuario());
            ps.setInt(2, amistad.getId_otro_usuario());
            ps.executeUpdate();
            ps.close();
        } catch (Exception ex) {
            Logger.getLogger(AmistadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
