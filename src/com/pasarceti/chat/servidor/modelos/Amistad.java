
package com.pasarceti.chat.servidor.modelos;

public class Amistad {
    
    private int id_usuario, id_otro_usuario;

    public Amistad() {
    }

    public Amistad(int id_usuario, int id_otro_usuario) {
        this.id_usuario = id_usuario;
        this.id_otro_usuario = id_otro_usuario;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public int getId_otro_usuario() {
        return id_otro_usuario;
    }

    public void setId_otro_usuario(int id_otro_usuario) {
        this.id_otro_usuario = id_otro_usuario;
    }

}
