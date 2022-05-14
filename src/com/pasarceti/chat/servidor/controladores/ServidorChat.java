package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.dto.DTOAbandonarGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOIdEntidad;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvAceptada;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOModInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @brief El servidor que realiza toda la comunicación con los clientes del chat.
*/
public class ServidorChat implements Runnable, PropertyChangeListener
{
    // El puerto del sistema en que está disponible este servidor.
    private final int puerto; 

    // La cantidad de hilos que va a utilizar esta instancia de servidor.
    private static final int NUM_HILOS = 100;

    // La cantidad máxima de eventos que pueden estar en queueEventos a la vez.
    private static final int MAX_EVENTOS_EN_QUEUE = 100;

    // Este Executor coordina y ejecuta todas las tareas de procesamiento de 
    // comunicación con sockets.
    private static final ExecutorService exec = Executors.newFixedThreadPool(NUM_HILOS);  
    
    // El servidor de sockets, usado para aceptar las conexiones.
    private ServerSocket socketServidor;

    // Servicio de logging para el servidor.
    private static final Logger logger = Logger.getLogger("ServidorChat");

    // Mantiene el estado del servidor en tiempo real, notifica a los listeners 
    // registrados cuando cambia alguna de sus propiedades.
    private final EstadoServidor estado = new EstadoServidor();

    // El queue para el patrón productor-consumidor (servidor y gui, en este caso)
    // que envía los eventos producidos por el servidor a los consumidores de este queue.
    private final BlockingQueue<Evento> queueEventos = new LinkedBlockingQueue<>(MAX_EVENTOS_EN_QUEUE);

    public ServidorChat(int puerto, Level nivelDeLogs) 
    {
        this.puerto = puerto;
        logger.setLevel(nivelDeLogs);
    }
    
    @Override
    public void run() 
    {
        ejecutar();
    }
    
    /**
     * @brief Ejecuta el servidor, haciendo que esté disponible y pueda recibir y 
     * enviar datos.
     */
    private void ejecutar() 
    {
        try 
        {
            logger.info(String.format(
                "Inciando servidor de chat en el puerto %s", 
                String.valueOf(puerto)
            ));
            
            // Registrar el servidor como listener de eventos, para poder notificar
            // a los clientes debidos.
            estado.agregarListener(this);
            
            socketServidor = new ServerSocket(puerto);

            logger.info("Servidor iniciado, esperando conexiones.");

            // Aceptar conexiones mientras el servicio de ejecucion siga activo.
            while (!exec.isShutdown()) 
            {   
                // Esperar a que haya una conexion de un cliente. Aceptarla cuando llegue.
                final Socket cliente = socketServidor.accept();

                // Timeout de 10 segundos para los read() al socket.
//                cliente.setSoTimeout(10000);

                logger.info("Cliente conectado");
                
                // Manejar la comunicación del cliente con una instancia de ManejadorClientes.
                Runnable tareaPeticion = new ManejadorClientes(cliente, estado, queueEventos);

                exec.execute(tareaPeticion);
            }

            logger.info("El servidor ya dejó de esperar conexiones.");        
        }
        catch (SocketException e) 
        {
            // Invocar socketServidor.close() produce esta excepcion, es importante
            // asegurar que hilo donde está corriendo "ejecutar()" se detenga y termine. 
            logger.log(Level.WARNING, "Excepción de socket: {0}", e.getMessage());
        }
        catch (IOException e) 
        {
            logger.log(Level.SEVERE, "Error iniciando el servidor: {0}", e.getMessage());
            detener();
        }
        finally 
        {
            estado.removerListener(this);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evento) 
    {
        final Gson gson = new Gson();
        EventoServidor notificacion = null;
        
        final ConcurrentHashMap<Integer, Socket> usuariosConectados = estado.getClientesConectados();
        
        final Set<Entry<Integer, Socket>> receptores = new HashSet<>();

        switch (evento.getPropertyName())
        {
        case EstadoServidor.PROP_USUARIOS_CONECTADOS:
            // Un cambio en los usuarios conectados es enviado a todos los demas
            // usuarios.
            receptores.addAll(usuariosConectados.entrySet());
            
            if (evento.getNewValue() != null) 
            {
                // Si el nuevo valor no es null, un usuario se conectó.
                notificacion = new EventoServidor(
                    TipoDeEvento.USUARIO_CONECTADO, 
                    gson.toJson(evento.getNewValue())
                );
            }
            else 
            {
                // Si el nuevo valor es null, un usuario se desconectó
                notificacion = new EventoServidor(
                    TipoDeEvento.USUARIO_DESCONECTADO, 
                    gson.toJson(evento.getOldValue())
                );
            }
            break;
        case EstadoServidor.PROP_MENSAJES_RECIBIDOS:
            if (evento.getNewValue() instanceof DTOMensaje)
            {
                final DTOMensaje mensaje = (DTOMensaje) evento.getNewValue();
                
                final Socket clienteDest = usuariosConectados.get(mensaje.getIdDestinatario());
                
                // Solo enviar el evento si el cliente del destinatario esta conectado.
                if (clienteDest != null)
                {
                    final Entry<Integer, Socket> destinatario = new AbstractMap.SimpleImmutableEntry<>(
                        mensaje.getIdDestinatario(),
                        clienteDest
                    );

                    // Si el mensaje fue agregado para este usuario, notificar con 
                    // los datos del nuevo mensaje.
                    receptores.add(destinatario);

                    final String jsonMensajeRecibido = gson.toJson(mensaje);

                    notificacion = new EventoServidor(
                        TipoDeEvento.MENSAJE_ENVIADO, 
                        jsonMensajeRecibido
                    );
                }
            }
            
            break;
        case EstadoServidor.PROP_INVITACIONES_RECIBIDAS:
            notificacion = eventoPorCambioEnInvitacion(evento, usuariosConectados, receptores);
            break;
        case EstadoServidor.PROP_GRUPOS_EXISTENTES:
            if (evento.getOldValue() instanceof Integer && evento.getNewValue() == null)
            {
                // El grupo fue eliminado.
                final int idGrupoEliminado = (int) evento.getOldValue();
                
                DTOGrupo grupo = estado.getGrupoPorId(idGrupoEliminado);
                
                if (grupo != null)
                {
                    grupo.getIdsUsuariosMiembro().forEach(idMiembro -> {
                        
                        final Socket socketMiembro = usuariosConectados.get(idMiembro);
                        
                        if (socketMiembro != null) 
                        {
                            // Agregar como receptores a todos los usuarios conectados 
                            // que forman parte del grupo.
                            receptores.add(new AbstractMap.SimpleImmutableEntry<>(
                                idMiembro,
                                socketMiembro
                            ));
                        }
                    });
                    
                    DTOIdEntidad objIdGrupo = new DTOIdEntidad(grupo.getId());
                    
                    String jsonIdGrupoElim = gson.toJson(objIdGrupo);
                    
                    notificacion = new EventoServidor(
                        TipoDeEvento.GRUPO_ELIMINADO, 
                        jsonIdGrupoElim
                    );
                }
            }
            else if (evento.getOldValue() instanceof DTOAbandonarGrupo && evento.getNewValue() == null)
            {
                // Un usuario abandono el grupo.
                final DTOAbandonarGrupo abandono = (DTOAbandonarGrupo) evento.getOldValue();
                
                // Enviar evento del servidor a cada miembro del grupo que este conectado.
                abandono.getGrupoAbandonado().getIdsUsuariosMiembro().forEach(idMiembro -> {
                        
                    final Socket socketMiembro = usuariosConectados.get(idMiembro);

                    if (socketMiembro != null) 
                    {
                        // Agregar como receptores a todos los usuarios conectados 
                        // que forman parte del grupo.
                        receptores.add(new AbstractMap.SimpleImmutableEntry<>(
                            idMiembro,
                            socketMiembro
                        ));
                    }
                });
                
                final DTOIdEntidad idUsuario = new DTOIdEntidad(abandono.getIdUsuarioQueAbandono());
                
                final String idUsuarioJson = gson.toJson(idUsuario);
                
                notificacion = new EventoServidor(
                    TipoDeEvento.USUARIO_ABANDONO_GRUPO, 
                    idUsuarioJson
                );
            }
            break;
        default:
            notificacion = new EventoServidor(
                TipoDeEvento.ERROR_SERVIDOR, 
                "El servidor iba a enviar un evento, pero se confundió."
            );
        }
        
        // Si existe un evento para notificar, enviarlo a todos los clientes 
        // receptores como notificacion.
        if (notificacion != null) 
        {
            final EventoServidor eventoServidor = notificacion;
            
            receptores.forEach(receptor -> {

                try 
                {
                    Socket cliente = receptor.getValue();
                    int idUsuario = receptor.getKey();
                    
                    // Usar el id de cada cliente al enviarle su notificación.
                    eventoServidor.setIdUsuarioCliente(idUsuario);
                    
                    ManejadorClientes.enviarEventoASocket(cliente, eventoServidor);
                } 
                catch (IOException e) 
                {
                    queueEventos.offer(new Evento(TipoDeEvento.ERROR_SERVIDOR, e.getMessage()));
                }
            });
        }
    }
    
    /**
     * Determina el tipo de evento producido por un cambio en las invitaciones y
     * produce un EventoServidor correspondiente.
     * 
     * @param evento El evento de cambio en las invitaciones.
     * @param usuariosConectados Los usuarios conectados actualmente.
     * @param receptores Los posibles receptores del EventoServidor producido.
     * @return El EventoServidor adecuado.
     */
    private EventoServidor eventoPorCambioEnInvitacion(
        PropertyChangeEvent evento, 
        ConcurrentHashMap<Integer, Socket> usuariosConectados,
        Set<Entry<Integer, Socket>> receptores
    ) 
    {
        final Gson gson = new Gson();
        EventoServidor notificacion = null;
        
        if (evento.getOldValue() instanceof DTOInvAceptada)
        {
            // La invitación de amistad o de grupo del usuario fue aceptada.
            final DTOInvitacion invitacion = ((DTOInvAceptada) evento.getOldValue()).getInvitacion();
            final DTOUsuario usuarioQueAcepto = ((DTOInvAceptada) evento.getOldValue()).getUsuarioQueAcepto();

            final String jsonOtroUsuario = gson.toJson(usuarioQueAcepto);
                
            // Solo enviar el evento si el cliente del destinatario esta conectado.       
            if (invitacion.esDeAmistad())
            {
                final Socket clienteQueInvito = usuariosConectados.get(invitacion.getIdUsuarioQueInvita());
                            
                if (clienteQueInvito != null)
                {
                    // El usuario tiene un nuevo amigo. Notificarle al usuario con 
                    // los datos del nuevo amigo.
                    receptores.add(new AbstractMap.SimpleImmutableEntry<>(
                        invitacion.getIdUsuarioQueInvita(),
                        clienteQueInvito
                    ));

                    notificacion = new EventoServidor(
                        TipoDeEvento.AMISTAD_ACEPTADA,
                        jsonOtroUsuario
                    );
                }
            }
            else 
            {
                DTOGrupo grupo = estado.getGrupoPorId(invitacion.getIdGrupo());
                
                // Enviar la notificacion si el grupo existe.
                if (grupo != null)
                {
                    grupo.getIdsUsuariosMiembro().forEach(idMiembro -> {
                        
                        final Socket socketMiembro = usuariosConectados.get(idMiembro);
                        
                        if (socketMiembro != null) 
                        {
                            // Agregar como receptores a todos los usuarios conectados 
                            // que forman parte del grupo.
                            receptores.add(new AbstractMap.SimpleImmutableEntry<>(
                                idMiembro,
                                socketMiembro
                            ));
                        }
                    });
                    
                    notificacion = new EventoServidor(
                        TipoDeEvento.USUARIO_SE_UNIO_A_GRUPO,
                        jsonOtroUsuario
                    );
                }
            }
        }
        else if (evento.getNewValue() instanceof DTOInvitacion && evento.getOldValue() == null)
        {
            final DTOInvitacion invitacion = (DTOInvitacion) evento.getNewValue();
            
            // La invitación fue enviada, notificar al usuario invitado.
            final Socket clienteInvitado = usuariosConectados.get(invitacion.getIdUsuarioInvitado());
            
            if (clienteInvitado != null)
            {
                receptores.add(new AbstractMap.SimpleImmutableEntry<>(
                    invitacion.getIdUsuarioInvitado(),
                    clienteInvitado
                ));
                
                final String jsonInvitacion = gson.toJson(invitacion);
                
                notificacion = new EventoServidor(
                    TipoDeEvento.INVITACION_ENVIADA,
                    jsonInvitacion
                );
            }
        }
        else if (evento.getNewValue() == null && evento.getOldValue() instanceof DTOInvitacion)
        {
            //TODO: Revisar si es necesario este evento.
            final DTOInvitacion invRechazada = (DTOInvitacion) evento.getOldValue();
            
            // La invitación fue enviada, notificar al usuario invitado.
            final Socket clienteQueInvita = usuariosConectados.get(invRechazada.getIdUsuarioQueInvita());
            
            // Entregar evento del servidor solo al cliente que envió la notificación, 
            // si es que está conectado.
            if (clienteQueInvita != null)
            {
                receptores.add(new AbstractMap.SimpleImmutableEntry<>(
                    invRechazada.getIdUsuarioQueInvita(),
                    clienteQueInvita
                ));
                
                final DTOModInvitacion idInvRechazada = new DTOModInvitacion(invRechazada.getId());
            
                final String jsonInvRechazada = gson.toJson(idInvRechazada);

                // La invitación fue rechazada.
                notificacion = new EventoServidor(
                    TipoDeEvento.AMISTAD_RECHAZADA,
                    jsonInvRechazada
                );
            }
        }
        
        return notificacion;
    }
    
    /**
     * Cierra el servidor de sockets, interrumpiendo cualquier comunicación pendiente.
     */
    public void detener()
    {
        if (socketServidor != null) 
        {
            try 
            {
                socketServidor.close();
                
            } catch (IOException e) 
            {
                logger.log(
                    Level.SEVERE, 
                    "Ocurri\u00f3 un error al detener el servidor: {0}", 
                    e.getMessage()
                );
            }
        }
    }

    /**
     * Intenta detener completamente al servidor "con gracia", evitando que acepte nuevas 
     * conexiones pero esperando a que las conexiones existentes terminen de 
     * ser procesadas.
     * 
     * @return <strong>true</strong> si el servidor fue detenido y todas las 
     * conexiones restantes fueron procesadas.
    */
    public boolean terminar() 
    {
        try 
        {            
            exec.shutdown();
            boolean execDetenidoSinTimeout = exec.awaitTermination(30, TimeUnit.SECONDS);
            
            if (socketServidor != null) 
            {
                socketServidor.close();
            }
            
            logger.info(execDetenidoSinTimeout 
                ? "Servidor terminado" 
                : "Tiempo de espera excedido al intentar terminar el servidor"
            );
            
            return exec.isTerminated();
            
        } catch (InterruptedException | IOException e) 
        {
            logger.log(
                Level.SEVERE, 
                "Ocurri\u00f3 un error al intentar terminar el servidor: {0}", 
                e.getMessage()
            );
        }
        
        return false;
    }

    public BlockingQueue<Evento> getQueueEventos()
    {
        return queueEventos;
    }
}
