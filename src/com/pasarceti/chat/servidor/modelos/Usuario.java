
package com.pasarceti.chat.servidor.modelos;

import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

public class Usuario {
    
    private int id;
    private String nombre_usuario, password;
    private List<Amistad> amigos;

    public List<Amistad> getAmigos() {
        return amigos;
    }

    //MARIANA QUE PEDO CON LA LISTA DE AMIGOS, SE QUEDA O SE VA?
    public void setAmigos(List<Amistad> amigos) {
        this.amigos = amigos;
    }

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
}
