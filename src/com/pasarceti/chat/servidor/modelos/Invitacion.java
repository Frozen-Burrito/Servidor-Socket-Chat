package com.pasarceti.chat.servidor.modelos;

public class Invitacion {

    private int id, id_usuario_invitado, id_grupo, id_usuario_emisor;

    public Invitacion() {
    }

    public Invitacion(int id, int id_usuario_invitado, int id_grupo, int id_usuario_emisor) {
        this.id = id;
        this.id_usuario_invitado = id_usuario_invitado;
        this.id_grupo = id_grupo;
        this.id_usuario_emisor = id_usuario_emisor;
    }

    public Invitacion(int id_usuario_invitado, int id_grupo, int id_usuario_emisor) {
        this.id_usuario_invitado = id_usuario_invitado;
        this.id_grupo = id_grupo;
        this.id_usuario_emisor = id_usuario_emisor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_usuario_invitado() {
        return id_usuario_invitado;
    }

    public void setId_usuario_invitado(int id_usuario_invitado) {
        this.id_usuario_invitado = id_usuario_invitado;
    }

    public int getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(int id_grupo) {
        this.id_grupo = id_grupo;
    }

    public int getId_usuario_emisor() {
        return id_usuario_emisor;
    }

    public void setId_usuario_emisor(int id_usuario_emisor) {
        this.id_usuario_emisor = id_usuario_emisor;
    }

}
