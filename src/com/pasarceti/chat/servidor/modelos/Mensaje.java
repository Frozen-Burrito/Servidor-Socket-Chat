package com.pasarceti.chat.servidor.modelos;

import java.sql.Date;

public class Mensaje {

    private String contenido;
    private int id, id_autor, id_dest_usuario, id_dest_grupo;
    private Date fecha;

    public Mensaje() {
    }

    // Constructor con todos los campos (incluyendo id)
    public Mensaje(int id, String contenido, int id_autor, int id_dest_usuario, int id_dest_grupo, Date fecha) {
        this.contenido = contenido;
        this.id = id;
        this.id_autor = id_autor;
        this.id_dest_usuario = id_dest_usuario;
        this.id_dest_grupo = id_dest_grupo;
        this.fecha = fecha;
    }

    // Constructor sin id
    public Mensaje(String contenido, int id_autor, int id_dest_usuario, int id_dest_grupo, Date fecha) {
        this.contenido = contenido;
        this.id_autor = id_autor;
        this.id_dest_usuario = id_dest_usuario;
        this.id_dest_grupo = id_dest_grupo;
        this.fecha = fecha;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_autor() {
        return id_autor;
    }

    public void setId_autor(int id_autor) {
        this.id_autor = id_autor;
    }

    public int getId_dest_usuario() {
        return id_dest_usuario;
    }

    public void setId_dest_usuario(int id_dest_usuario) {
        this.id_dest_usuario = id_dest_usuario;
    }

    public int getId_dest_grupo() {
        return id_dest_grupo;
    }

    public void setId_dest_grupo(int id_dest_grupo) {
        this.id_dest_grupo = id_dest_grupo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
