/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos.dto;

import java.time.LocalDateTime;

/**
 *
 * 
 */
public class DTOMensaje 
{
    private int idMensaje;

    private String contenido;

    private LocalDateTime fecha;

    private int tipoDestinatario;

    private transient int idAutor;

    private int idDestinatario;

    public DTOMensaje(String contenido, int tipoDestinatario, int idDestinatario) {
        this.idMensaje = -1;
        this.fecha = LocalDateTime.now();
        this.contenido = contenido;
        this.tipoDestinatario = tipoDestinatario;
        this.idDestinatario = idDestinatario;
        this.idAutor = -1;
    }

    public void setIdMensaje(int idMensaje) {
        this.idMensaje = idMensaje;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }



    public int getIdMensaje() {
        return idMensaje;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getTipoDestinatario() {
        return tipoDestinatario;
    }

    public int getIdAutor() {
        return idAutor;
    }

    public int getIdDestinatario() {
        return idDestinatario;
    }


}
