
package com.pasarceti.chat.servidor.modelos;

import com.pasarceti.chat.servidor.bd.ControladorBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuariosGrupoDAO extends ControladorBD {

 public UsuariosGrupoDAO() {
        super();
    }

    // Añadir nueva relacion entre un usuario y un grupo
    public void crear(UsuariosGrupo usuarios_grupo) {
        PreparedStatement ps;
        try {
            ps = getC().prepareStatement("INSERT INTO usuariosgrupo VALUES(?,?)");
            ps.setInt(1, usuarios_grupo.getId_usuario_miembro());
            ps.setInt(2, usuarios_grupo.getId_grupo());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosGrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Buscar una lista de relacion usuarios-grupo de un dado USUARIO
    public List<UsuariosGrupo> busqueda_porUsuario(Usuario usuario) {
        return busqueda_porIdUsuario(usuario.getId());
    }

    // Buscar una lista de relacion usuarios-grupo de un dado USUARIO con un ID
    public List<UsuariosGrupo> busqueda_porIdUsuario(int idUsuario) {
        PreparedStatement ps;
        ResultSet res;
        List<UsuariosGrupo> ugs = new ArrayList<>();
        try {
            ps = getC().prepareStatement("SELECT * from usuariosgrupo WHERE id_usuario_miembro = ?");
            ps.setInt(1, idUsuario);
            res = ps.executeQuery();
            while (res.next()) {
                UsuariosGrupo ug = new UsuariosGrupo();
                ug.setId_usuario_miembro(res.getInt("id_usuario_miembro"));
                ug.setId_grupo(res.getInt("id_grupo"));
                ugs.add(ug);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosGrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ugs;
    }
    
    // Buscar una lista de relacion usuarios-grupo de un dado GRUPO
    public List<UsuariosGrupo> busqueda_porGrupo(Grupo grupo) {
        return busqueda_porGrupo(grupo.getId());
    }

    // Buscar una lista de relacion usuarios-grupo de un dado ID de GRUPO
    public List<UsuariosGrupo> busqueda_porGrupo(int idGrupo) {
        PreparedStatement ps;
        ResultSet res;
        List<UsuariosGrupo> ugs = new ArrayList<>();
        try {
            ps = getC().prepareStatement("SELECT * from usuariosgrupo WHERE id_grupo = ?");
            ps.setInt(1, idGrupo);
            res = ps.executeQuery();
            while (res.next()) {
                UsuariosGrupo ug = new UsuariosGrupo();
                ug.setId_usuario_miembro(res.getInt("id_usuario_miembro"));
                ug.setId_grupo(res.getInt("id_grupo"));
                ugs.add(ug);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosGrupoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ugs;
    }

    // Eliminar relacion usuario-grupo
    public void eliminar(UsuariosGrupo ug) {
        PreparedStatement ps;
        try {
            ps = getC().prepareStatement("DELETE FROM usuariosgrupo WHERE id_grupo = ? AND id_usuario_miembro = ?");
            ps.setInt(1, ug.getId_grupo());
            ps.setInt(2, ug.getId_usuario_miembro());
            ps.executeUpdate();
        } catch (Exception ex) {
            Logger.getLogger(AmistadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
