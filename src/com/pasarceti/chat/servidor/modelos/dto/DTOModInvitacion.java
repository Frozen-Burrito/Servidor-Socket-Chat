/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos.dto;

/**
 *
 * 
 */
public class DTOModInvitacion 
{
    private final int idInvitacion;

    public DTOModInvitacion(int idInvitacion) {
        this.idInvitacion = idInvitacion;
    }

    public int getIdInvitacion() {
        return idInvitacion;
    }

    public String toString() {
        return String.valueOf(idInvitacion);
    }
}
