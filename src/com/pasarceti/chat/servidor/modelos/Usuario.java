package com.pasarceti.chat.servidor.modelos;

import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

public class Usuario {

    private int id;
    private String nombre_usuario, password;
    private List<Amistad> amigos;
    private List<UsuariosGrupo> grupos;
    private Boolean conectado;

    public Usuario() {
    }

    public Usuario(String nombre_usuario, String password) {
        password = DigestUtils.sha256Hex(password);
        this.nombre_usuario = nombre_usuario;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPasswordSecure(String password) {
        password = DigestUtils.sha256Hex(password);
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public List<Amistad> getAmigos() {
//        AmistadDAO amistad = new AmistadDAO();
//        amigos = amistad.busqueda_porUsuario(id);
//        return amigos;
//    }

    public void setAmigos(List<Amistad> amigos) {
        this.amigos = amigos;
    }

//    public List<UsuariosGrupo> getGrupos() {
//        UsuariosGrupoDAO grupo = new UsuariosGrupoDAO();
//        grupos = grupo.busqueda_porUsuario(this);
//        return grupos;
//    }

    public void setGrupos(List<UsuariosGrupo> grupos) {
        this.grupos = grupos;
    }

    public Boolean getConectado() {
        return conectado;
    }

    public void setConectado(Boolean conectado) {
        this.conectado = conectado;
    }

}
