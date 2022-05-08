package com.pasarceti.chat.servidor;

import java.io.IOException;
import com.pasarceti.chat.servidor.controladores.ServidorChat;

public class Main {

    public static void main(String[] args) {
        
        // Crear nueva instancia de servidor, escuchando en el puerto 9999.
        ServidorChat servidor = new ServidorChat(9999);
        try 
        {
            // Comenzar la ejecución del servidor.
            servidor.ejecutar();

        } catch (IOException e) {
            System.out.println("Error IOException: " + e.getMessage());
        }
    }

}
