/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class DTOGrupo extends DTODestinatario 
{
    private int idUsuario;

    private final String nombre;

    private  List<Integer> idsUsuariosMiembro = new ArrayList<>();

    public DTOGrupo(int idUsuario, String nombre, List<Integer> idsUsuarios) 
    {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.idsUsuariosMiembro = idsUsuarios;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Integer> getIdsUsuariosMiembro() {
        return idsUsuariosMiembro;
    }
}
