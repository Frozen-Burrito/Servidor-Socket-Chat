/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos.dto;

/**
 * Un objeto que transporta el ID de una entidad, usualmente para transformarlo
 * en un objeto JSON.
 * 
 */
public class DTOIdEntidad
{
    private int id;

    public DTOIdEntidad(int id) {
        this.id = id;
    }

    public int getIdGrupo() {
        return id;
    }
}
