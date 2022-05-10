/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.controladores;

import com.pasarceti.chat.servidor.modelos.Comunicacion;

/**
 * @brief Interpreta el contenido de una peticion, o transforma objetos de Java 
 * a una respuesta válida.
 * 
 */
public class InterpreteDeComunicacion 
{   
    public static Comunicacion interpretarPeticion(String peticion) 
    {
        return new Comunicacion(0, true, 0, peticion);
    }

    public static String formarRespuesta(Comunicacion respuesta) 
    {
        return respuesta.toString();
    }
}
