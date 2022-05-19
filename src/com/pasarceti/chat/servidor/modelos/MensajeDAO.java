package com.pasarceti.chat.servidor.modelos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MensajeDAO {
    
    private final Connection conexionBD;
    
    public MensajeDAO(Connection conexionBD) {        
        this.conexionBD = conexionBD; 
    }

    // Crear mensaje para otro usuario
    public int crear_paraUsuario(Mensaje mensaje) {
        PreparedStatement ps;
        try {
            String query = "INSERT INTO mensaje (contenido, fecha, id_autor, id_dest_usuario) VALUES(?,?,?,?)";
            ps = conexionBD.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, mensaje.getContenido());
            
            Timestamp fecha = Timestamp.valueOf(mensaje.getFecha());
            ps.setTimestamp(2, fecha);
            
            ps.setInt(3, mensaje.getId_autor());
            ps.setInt(4, mensaje.getId_dest_usuario());
            ps.executeUpdate();
            
            ResultSet resultados = ps.getGeneratedKeys();
            
            if (resultados.next())
            {
                return resultados.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    // Crear mensaje para grupo
    public void crear_paraGrupo(Mensaje mensaje) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("INSERT INTO mensaje (contenido, fecha, id_autor, id_dest_grupo) VALUES(?,?,?,?)");
            ps.setString(1, mensaje.getContenido());
            
            Timestamp fecha = Timestamp.valueOf(mensaje.getFecha());
            ps.setTimestamp(2, fecha);
            
            ps.setInt(3, mensaje.getId_autor());
            ps.setInt(4, mensaje.getId_dest_grupo());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    // Buscar una lista de mensajes enviados a un usuario
    public List<Mensaje> busqueda_DestUsuario(Usuario usuario) {
        PreparedStatement ps;
        ResultSet res;
        List<Mensaje> mensajes = new ArrayList<>();
        try {
            ps = conexionBD.prepareStatement("SELECT * from mensaje WHERE id_dest_usuario = ?");
            ps.setInt(1, usuario.getId());
            res = ps.executeQuery();
            while (res.next()) {
                Mensaje mensaje = new Mensaje();
                mensaje.setId(res.getInt("id"));
                mensaje.setContenido(res.getString("contenido"));
                
                Date fecha = res.getDate("fecha");
                Timestamp t = new Timestamp(fecha.getTime());
                mensaje.setFecha(t.toLocalDateTime());
                
                mensaje.setId_autor(res.getInt("id_autor"));
                mensaje.setId_dest_grupo(res.getInt("id_dest_grupo"));
                mensaje.setId_dest_usuario(res.getInt("id_dest_usuario"));
                mensajes.add(mensaje);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mensajes;
    }


    // Buscar una lista de mensajes enviados a un grupo
    public List<Mensaje> busqueda_porGrupo(Grupo grupo) {
        PreparedStatement ps;
        ResultSet res;
        List<Mensaje> mensajes = new ArrayList<>();
        try {
            ps = conexionBD.prepareStatement("SELECT * from mensaje WHERE id_dest_grupo = ?");
            ps.setInt(1, grupo.getId());
            res = ps.executeQuery();
            while (res.next()) {
                Mensaje mensaje = new Mensaje();
                mensaje.setId(res.getInt("id"));
                mensaje.setContenido(res.getString("contenido"));
                
                Date fecha = res.getDate("fecha");
                Timestamp t = new Timestamp(fecha.getTime());
                mensaje.setFecha(t.toLocalDateTime());
                
                mensaje.setId_autor(res.getInt("id_autor"));
                mensaje.setId_dest_grupo(res.getInt("id_dest_grupo"));
                mensaje.setId_dest_usuario(res.getInt("id_dest_usuario"));
                mensajes.add(mensaje);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mensajes;
    }

    // Eliminar mensaje
    public void eliminar(Mensaje mensaje) {
        PreparedStatement ps;
        try {
            ps = conexionBD.prepareStatement("DELETE FROM mensaje WHERE id = ?");
            ps.setInt(1, mensaje.getId());
            ps.executeUpdate();
        } catch (Exception ex) {
            Logger.getLogger(MensajeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
