package com.pasarceti.chat.servidor.modelos.dto;

import com.pasarceti.chat.servidor.modelos.Usuario;
import java.util.List;

/**
 * La lista completa de contactos de un usuario. Incluye amistades, grupos y 
 * usuarios conectados.
 * 
 * <strong>Esta clase evita que se repitan registros (que las listas de amigos y 
 * de usuarios conectados contengan el mismo usuario, por ejemplo.)</strong>
 * 
 * La lista de grupos contiene cada grupo, con los IDs
 * de los miembros 
 */
public class DTOContactos
{
    private final DTOUsuario usuario;
    
    // Contiene a todos los usuarios amigos del usuario.
    private final List<DTOUsuario> amigos;
    
    // Contiene a todos los usuarios conectados que no sean amigos del usuario.
    private final List<DTOUsuario> usuariosConectados;
    
    // Contiene los datos de los grupos del usuario, además de los IDs de los miembros.
    private final List<DTOGrupo> grupos;
    
    private final List<DTOUsuario> miembrosDesconectados;

    public DTOContactos(
            DTOUsuario usuario,
            List<DTOUsuario> amigos, 
            List<DTOUsuario> usuariosConectados, 
            List<DTOGrupo> grupos, 
            List<DTOUsuario> miembrosDesconectados
    )
    {
        this.usuario = usuario;
        this.amigos = amigos;
        this.usuariosConectados = usuariosConectados;
        this.grupos = grupos;
        this.miembrosDesconectados = miembrosDesconectados;
    }

    public List<DTOUsuario> getAmigos()
    {
        return amigos;
    }

    public List<DTOUsuario> getUsuariosConectados()
    {
        return usuariosConectados;
    }

    public List<DTOGrupo> getGrupos()
    {
        return grupos;
    }

    public List<DTOUsuario> getMiembrosDesconectados()
    {
        return miembrosDesconectados;
    }
}
