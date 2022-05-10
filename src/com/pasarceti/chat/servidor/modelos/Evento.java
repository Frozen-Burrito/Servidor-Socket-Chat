/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.modelos;

import java.time.LocalDateTime;

/**
 * Un evento del servidor chat, es enviado por cada accion de un cliente.
 * 
 */
public class Evento 
{
    private final TipoDeEvento tipo;

    private LocalDateTime fecha;

    private final String datos;

    public Evento(TipoDeEvento tipo, String datos) {
        this.tipo = tipo;
        this.datos = datos;
    }

    public Evento(TipoDeEvento tipo, LocalDateTime fecha, String datos) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.datos = datos;
    }

    @Override
    public String toString() {
        return String.format("[%s] - %s: %s", fecha.toString(), tipo.toString(), datos);
    }
}
