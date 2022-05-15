package com.pasarceti.chat.servidor;

import com.pasarceti.chat.servidor.bd.ControladorBD;
import com.pasarceti.chat.servidor.modelos.Amistad;
import com.pasarceti.chat.servidor.modelos.AmistadDAO;
import com.pasarceti.chat.servidor.modelos.Grupo;
import com.pasarceti.chat.servidor.modelos.GrupoDAO;
import com.pasarceti.chat.servidor.modelos.Invitacion;
import com.pasarceti.chat.servidor.modelos.InvitacionDAO;
import com.pasarceti.chat.servidor.modelos.Mensaje;
import com.pasarceti.chat.servidor.modelos.MensajeDAO;
import com.pasarceti.chat.servidor.modelos.Usuario;
import com.pasarceti.chat.servidor.modelos.UsuarioDAO;
import com.pasarceti.chat.servidor.modelos.UsuariosGrupo;
import com.pasarceti.chat.servidor.modelos.UsuariosGrupoDAO;
import java.sql.Date;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

public class Main {

    public static void main(String[] args) {

    }

    private static void pruebas_mensaje() {
        /*
        MensajeDAO mensajeDAO = new MensajeDAO();
        Date fecha = new Date(11, 11, 2022);
        Mensaje mensaje = new Mensaje("Holaaaaaa", 1, 3, 0, fecha);
        //  ugDAO.crear(ug);
        //mensajeDAO.crear_paraUsuario(mensaje);

        UsuarioDAO crud = new UsuarioDAO();
        Usuario user = crud.busqueda_porNombre("usuario3");
        /*
        GrupoDAO grupoDAO = new GrupoDAO();
        Grupo grupo = grupoDAO.buscar_porId(1);

        List<Mensaje> mensajes = mensajeDAO.busqueda_DestUsuario(user);
        for (int i = 0; i < mensajes.size(); i++) {
            System.out.println("Mensajes enviados a usuario. Id usuario destinatario: " + mensajes.get(i).getId_dest_usuario() + " Desde usuario: " + mensajes.get(i).getId_autor());
        }
         */
    }

    private static void pruebas_usuario() {
        UsuarioDAO crud = new UsuarioDAO();

        Usuario user = new Usuario("usuario1", "aaa");
        Usuario user2 = new Usuario("usuario2", "aaa");

        crud.crear(user);
        crud.crear(user2);
        /*
        String parsedPassword = DigestUtils.sha256Hex("aaa");
        Usuario usuario = crud.read_withCredentials("aaaa", parsedPassword);
        
        usuario.setNombre_usuario("aaaa");
        crud.delete(usuario);
         */
    }

    private static void pruebas_grupo() {
        GrupoDAO grupoDAO = new GrupoDAO();
        Grupo grupo = new Grupo("Grupo1");
        grupo = grupoDAO.buscar_porId(1);
        //grupoDAO.crear(grupo);

        grupoDAO.eliminar(grupo);
    }

    private static void puebas_amistad() {

        AmistadDAO amistadCRUD = new AmistadDAO();
        Amistad amistad = new Amistad(1, 3);
        amistadCRUD.crear(amistad);
        Amistad amistad2 = new Amistad(3, 1);
        amistadCRUD.crear(amistad2);
        /*
        List<Amistad> amistades = amistadCRUD.busqueda_porUsuario(amistad);
        for (int i = 0; i < amistades.size(); i++) {
            System.out.println("usuario " + i + ": " + amistades.get(i).getId_usuario());
            System.out.println("otro usuario " + i + ": " + amistades.get(i).getId_otro_usuario());
        }
         */
 /*
        amistad = amistadCRUD.read(9, 3);
        if (amistad == null) {
            System.out.println("NO existe!");
        }
        else
            System.out.println(amistad.getId_otro_usuario());            
         */
        // pruebas_usuario();
    }

    private static void puebas_invitacion() {
        InvitacionDAO invitacionDAO = new InvitacionDAO();

        // CREAR INVITACIONES 
/*
        Invitacion invitacion = new Invitacion(2, 0, 1);
        invitacionDAO.crear_paraAmistad(invitacion);

        Invitacion invitacion2 = new Invitacion(2, 1, 1);
        invitacionDAO.crear_paraGrupo(invitacion2);
         */

        // IMPRIMIR LISTA DE INVITACIONES SEGUN USUARIO
        UsuarioDAO crud = new UsuarioDAO();
        Usuario user = crud.busqueda_porNombre("usuario2");
        System.out.println("Usuario Id: " + user.getId());

        List<Invitacion> invitaciones = invitacionDAO.busqueda_porUsuarioEmisor(user);
        if (!invitaciones.isEmpty()) {
            for (int i = 0; i < invitaciones.size(); i++) {
                System.out.println("Invitacion. Id: " + invitaciones.get(i).getId() + " Grupo: " + invitaciones.get(i).getId_grupo()
                        + " Usuario EMISOR: " + invitaciones.get(i).getId_usuario_emisor()
                        + " Usuario INVITADO: " + invitaciones.get(i).getId_usuario_invitado());
            }
        }

    }

    private static void puebas_usuarios_grupo() {
        UsuariosGrupoDAO ugDAO = new UsuariosGrupoDAO();
        UsuariosGrupo ug = new UsuariosGrupo(1, 3);
        //  ugDAO.crear(ug);

        UsuarioDAO crud = new UsuarioDAO();
        Usuario user = crud.busqueda_porNombre("usuario1");

        GrupoDAO grupoDAO = new GrupoDAO();
        Grupo grupo = grupoDAO.buscar_porId(1);

        List<UsuariosGrupo> ugs = ugDAO.busqueda_porGrupo(grupo);
        for (int i = 0; i < ugs.size(); i++) {
            System.out.println("Usuarios grupo. Id usuario: " + ugs.get(i).getId_usuario_miembro() + " Id Grupo: " + ugs.get(i).getId_grupo());
        }

        ugDAO.eliminar(ug);

    }

}
