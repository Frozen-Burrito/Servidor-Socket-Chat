
package com.pasarceti.chat.servidor.modelos;

public class UsuariosGrupo {

    private int id_usuario_miembro, id_grupo;

    public UsuariosGrupo() {
    }

    public UsuariosGrupo(int id_usuario_miembro, int id_grupo) {
        this.id_usuario_miembro = id_usuario_miembro;
        this.id_grupo = id_grupo;
    }

    public int getId_usuario_miembro() {
        return id_usuario_miembro;
    }

    public void setId_usuario_miembro(int id_usuario_miembro) {
        this.id_usuario_miembro = id_usuario_miembro;
    }

    public int getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(int id_grupo) {
        this.id_grupo = id_grupo;
    }    

}
