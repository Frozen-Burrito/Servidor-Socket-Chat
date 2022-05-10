package com.pasarceti.chat.servidor;

import com.pasarceti.chat.servidor.controladores.ServidorChat;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        // Crear nueva instancia de servidor, escuchando en el puerto 9999.
        ServidorChat servidor = new ServidorChat(9998);

        // Comenzar la ejecución del servidor.
        servidor.ejecutar();
    }
}
