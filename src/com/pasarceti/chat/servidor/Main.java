package com.pasarceti.chat.servidor;

import javax.swing.SwingUtilities;

import com.pasarceti.chat.servidor.controladores.ServidorChat;
import com.pasarceti.chat.servidor.bd.CredencialesBD;
import com.pasarceti.chat.servidor.bd.PoolConexionesBD;
import com.pasarceti.chat.servidor.gui.InterfazGrafica;
import com.pasarceti.chat.servidor.gui.WorkerEventos;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        final InterfazGrafica gui = new InterfazGrafica();
        
        WorkerEventos swingWorkerEventos = null;
        
        try {
            CredencialesBD credenciales = getCredencialesBD();
            
            final PoolConexionesBD poolDeConexiones = new PoolConexionesBD(credenciales);
            
            // Configurar el servidor de chat.
            ServidorChat servidor = new ServidorChat(9998, Level.INFO, poolDeConexiones);

            // Configurar el GUI y las acciones.
            WorkerEventos workerEventos = configurarGUIConServidor(gui, servidor);

            SwingUtilities.invokeLater(() -> {
                // Iniciar a ejecutar la interfaz
                gui.Inicio();
                workerEventos.execute();
                gui.setVisible(true);
            });
        } catch (SQLException ex) 
        {
            SwingUtilities.invokeLater(() -> {
                // Mostrar dialog cuando hay un error de conexion a la BD.
                gui.mostrarAlerta("El servidor no pudo conectarse con la base de datos.");
            });
        }
    }
    
    public static CredencialesBD getCredencialesBD()
    {
        try 
        {
            CredencialesBD credenciales = CredencialesBD.desdeArchivoConfig("config_servidor.txt");
            
            return credenciales;
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) 
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static WorkerEventos configurarGUIConServidor(InterfazGrafica gui, ServidorChat servidor)
    {
        gui.ConfigurarBtnON((ActionEvent e) -> {
            if (servidor.estaDetenido())
            {
                gui.ActBtnOn();
                servidor.reiniciar();
            }
        });

        gui.ConfigurarBtnOFF((ActionEvent e) -> {
            if (!servidor.estaDetenido())
            {
                gui.ActBtnOFF();
                servidor.interrumpir();
            }
        });

        return new WorkerEventos(servidor.getQueueEventos(), gui);
    }
}
