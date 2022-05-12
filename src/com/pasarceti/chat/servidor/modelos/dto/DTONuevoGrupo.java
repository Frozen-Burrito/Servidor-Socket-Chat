package com.pasarceti.chat.servidor.modelos.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class DTONuevoGrupo extends DTODestinatario 
{
    private final int idGrupo;

    private final String nombre;

    private List<Integer> idsUsuariosMiembro = new ArrayList<>();

    public DTONuevoGrupo(int idGrupo, String nombre, List<Integer> idsUsuarios) 
    {
        this.idGrupo = idGrupo;
        this.nombre = nombre;
        this.idsUsuariosMiembro = idsUsuarios;
    }

    public int getId() {
        return idGrupo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Integer> getIdsUsuariosMiembro() {
        return idsUsuariosMiembro;
    }
}
