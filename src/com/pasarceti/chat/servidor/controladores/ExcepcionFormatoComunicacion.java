/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.controladores;

/**
 * Representa un error en el formato de un objeto de comunicación.
 *
 * (El servidor no puede interpretar la petición del cliente porque no incluye
 * cierto dato, por ejemplo)
 */
public class ExcepcionFormatoComunicacion extends Exception {

    public ExcepcionFormatoComunicacion(String mensaje) 
    {
        super(mensaje);
    }
}
