package com.pasarceti.chat.servidor.modelos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {

    private final Connection conexionBD;
    
    public UsuarioDAO(Connection conexionBD) {
       
        this.conexionBD = conexionBD; 
    }

    // Añadir un nuevo usuario
    public int crear(Usuario usuario) {
        PreparedStatement ps;
        try {
            String query = "INSERT INTO usuario (nombre_usuario, password) VALUES(?,?)";
            ps = conexionBD.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, usuario.getNombre_usuario());
            ps.setString(2, usuario.getPassword());
            ps.executeUpdate();
            
            ResultSet resultados = ps.getGeneratedKeys();
            
            if (resultados.next())
            {
                return resultados.getInt(1);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    // Buscar un usuario que tenga el mismo nombre y password que las puestas en inicio de sesion
    public Usuario busqueda_porCredenciales(String nombre_usuario, String password) {
        PreparedStatement ps;
        Usuario usuario = null;
        ResultSet res;
        try {
            ps = conexionBD.prepareStatement("SELECT * from usuario WHERE nombre_usuario = ? AND password = ?");
            ps.setString(1, nombre_usuario);
            ps.setString(2, password);
            res = ps.executeQuery();
            if (res.next()) {
                
                usuario = new Usuario();
                usuario.setId(res.getInt("id"));
                usuario.setNombre_usuario(res.getString("nombre_usuario"));
                usuario.setPassword(res.getString("password"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usuario;
    }
    
    // Buscar un usuario que tenga el mismo id que el dado.
    public Usuario busqueda_porId(int id_usuario) {
        PreparedStatement ps;
        Usuario usuario = null;
        ResultSet res;
        try {
            ps = conexionBD.prepareStatement("SELECT * from usuario WHERE id = ?");
            ps.setInt(1, id_usuario);
            res = ps.executeQuery();
            if (res.next()) {
                
                usuario = new Usuario();
                usuario.setId(res.getInt("id"));
                usuario.setNombre_usuario(res.getString("nombre_usuario"));
                usuario.setPassword(res.getString("password"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usuario;
    }

    // Buscar un usuario cuyo nombre coincida con el dado
    public Usuario busqueda_porNombre(String nombre_usuario) {
        PreparedStatement ps;
        ResultSet res;
        Usuario usuario = null;
        try {
            ps = conexionBD.prepareStatement("SELECT * from usuario WHERE nombre_usuario = ?");
            ps.setString(1, nombre_usuario);
            res = ps.executeQuery();
            if (res.next()) {
                
                usuario = new Usuario();
                usuario.setId(res.getInt("id"));
                usuario.setNombre_usuario(res.getString("nombre_usuario"));
                usuario.setPassword(res.getString("password"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usuario;
    }

    // Actualizar datos del usuario
    public void actualizar(Usuario usuario) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("UPDATE usuario SET password = ? WHERE id = ?");
            ps.setString(1, usuario.getPassword());
            ps.setInt(2, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Eliminar usuario
    public void eliminar(Usuario usuario) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("DELETE FROM usuario WHERE id = ?");
            ps.setInt(1, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
