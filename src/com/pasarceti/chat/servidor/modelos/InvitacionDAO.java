package com.pasarceti.chat.servidor.modelos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvitacionDAO {

    private final Connection conexionBD;
    
    public InvitacionDAO(Connection conexionBD) {        
        this.conexionBD = conexionBD; 
    }

    // Crear invitación para entablar amistad
    public int crear_paraAmistad(Invitacion invitacion) {
        PreparedStatement ps;
        try {
            String query = "INSERT INTO invitacion (id_usuario_invitado, id_usuario_emisor) VALUES(?,?)";
            ps = conexionBD.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invitacion.getId_usuario_invitado());
            ps.setInt(2, invitacion.getId_usuario_emisor());
            ps.executeUpdate();
            
            ResultSet resultados = ps.getGeneratedKeys();
            
            if (resultados.next()) {
                return resultados.getInt(1);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(InvitacionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    // Crear invitación para grupo
    public int crear_paraGrupo(Invitacion invitacion) {
        PreparedStatement ps;
        try {
            String query = "INSERT INTO invitacion (id_usuario_invitado, id_grupo, id_usuario_emisor) VALUES(?,?)";
            ps = conexionBD.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invitacion.getId_usuario_invitado());
            ps.setInt(2, invitacion.getId_grupo());
            ps.setInt(3, invitacion.getId_usuario_emisor());
            ps.executeUpdate();
            
            ResultSet resultados = ps.getGeneratedKeys();
            
            if (resultados.next()) {
                return resultados.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvitacionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    // Buscar una invitacion por id
    public Invitacion busqueda_porId(int id) {
        PreparedStatement ps;
        ResultSet res;
        Invitacion invitacion = new Invitacion();
        try {
            ps = conexionBD.prepareStatement("SELECT * from invitacion WHERE id = ?");
            ps.setInt(1, id);
            res = ps.executeQuery();
            if (res.next()) {
                invitacion.setId(res.getInt("id"));
                invitacion.setId_grupo(res.getInt("id_grupo"));
                invitacion.setId_usuario_invitado(res.getInt("Id_usuario_invitado"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvitacionDAO.class.getName()).log(Level.SEVERE, null, ex);
            invitacion = null;
        }
        return invitacion;
    }

    // Buscar una lista de invitaciones enviadas DESDE un usuario
    public List<Invitacion> busqueda_porUsuarioEmisor(Usuario usuario) {
        PreparedStatement ps;
        ResultSet res;
        List<Invitacion> invitaciones = new ArrayList<>();
        try {
            ps = conexionBD.prepareStatement("SELECT * from invitacion WHERE id_usuario_emisor = ?");
            ps.setInt(1, usuario.getId());
            res = ps.executeQuery();
            while (res.next()) {
                Invitacion invitacion = new Invitacion();
                invitacion.setId(res.getInt("id"));
                invitacion.setId_usuario_invitado(res.getInt("id_usuario_invitado"));
                invitacion.setId_grupo(res.getInt("id_grupo"));
                invitacion.setId_usuario_emisor(res.getInt("id_usuario_emisor"));
                invitaciones.add(invitacion);
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvitacionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return invitaciones;
    }


    // Buscar una lista de invitaciones enviadas A un usuario
    public List<Invitacion> busqueda_porUsuarioInvitado(Usuario usuario) {
        PreparedStatement ps;
        ResultSet res;
        List<Invitacion> invitaciones = new ArrayList<>();
        try {
            ps = conexionBD.prepareStatement("SELECT * from invitacion WHERE id_usuario_invitado = ?");
            ps.setInt(1, usuario.getId());
            res = ps.executeQuery();
            while (res.next()) {
                Invitacion invitacion = new Invitacion();
                invitacion.setId(res.getInt("id"));
                invitacion.setId_usuario_invitado(res.getInt("id_usuario_invitado"));
                invitacion.setId_grupo(res.getInt("id_grupo"));
                invitacion.setId_usuario_emisor(res.getInt("id_usuario_emisor"));
                invitaciones.add(invitacion);
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvitacionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return invitaciones;
    }

    // Eliminar invitacion
    public void eliminar(Invitacion invitacion) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("DELETE FROM invitacion WHERE id = ?");
            ps.setInt(1, invitacion.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(InvitacionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
