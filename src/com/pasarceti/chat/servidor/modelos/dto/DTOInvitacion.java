/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos.dto;

/**
 *
 * 
 */
public class DTOInvitacion 
{
    private int idGrupo;

    private int idUsuarioInvitado;

    public DTOInvitacion(int idGrupo, int idUsuarioInvitado) {
        this.idGrupo = idGrupo;
        this.idUsuarioInvitado = idUsuarioInvitado;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public int getIdUsuarioInvitado() {
        return idUsuarioInvitado;
    }
}
