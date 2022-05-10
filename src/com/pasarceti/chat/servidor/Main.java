package com.pasarceti.chat.servidor;

import com.pasarceti.chat.servidor.controladores.ServidorChat;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        // Crear nueva instancia de servidor, escuchando en el puerto 9999.
        ServidorChat servidor = new ServidorChat(9998);

/*
        Para que el servidor "envíe" una notificación de un evento (mensaje enviado, registro,
        etc.) al GUI, decidi hacerlo con Observable. El servidor puede ser Observable y el
        GUI seria el Observer. Cuando el servidor registra un evento, notifica a todos sus
        observadores (el GUI en este caso). Los observadores responden al cambio en sus propios
        metodos update().

        Observer gui = new Gui(); // Crear una nueva instancia del GUI

        servidor.addEventObserver(gui); // Agregar la GUI como observadora del servidor.
*/
        // Comenzar la ejecución del servidor.
        servidor.ejecutar();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //TODO: Iniciar la ejecución del GUI.
//                gui.show(); // Quizas algo asi?
            }
        });
    }
}
