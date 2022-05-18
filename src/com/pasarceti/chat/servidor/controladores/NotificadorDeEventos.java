package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import com.pasarceti.chat.servidor.modelos.Cliente;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.dto.DTOAbandonarGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOIdEntidad;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvAceptada;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.util.Collection;

/**
 * Escucha los cambios producidos en las propiedades del EstadoServidor y envía
 * notificaciones a los clientes que deban recibirlas.
 */
public class NotificadorDeEventos implements PropertyChangeListener
{
    private final Gson gson = new Gson();
    
    private final EstadoServidor estadoServidor;
    
    private final BlockingQueue<Evento> queueEventos;
    
    public NotificadorDeEventos(EstadoServidor estado, BlockingQueue<Evento> queueEventos)
    {
        this.estadoServidor = estado;
        this.queueEventos = queueEventos;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evento)
    {
        EventoServidor notificacion;
        
        switch (evento.getPropertyName())
        {
        case EstadoServidor.PROP_USUARIOS_CONECTADOS:
            notificacion = eventoPorUsuariosConectados(evento);
            break;
        case EstadoServidor.PROP_MENSAJES_RECIBIDOS:
            notificacion = eventoPorMensajeRecibido(evento);
            break;
        case EstadoServidor.PROP_INVITACIONES_RECIBIDAS:
            notificacion = eventoPorCambioEnInvitacion(evento);
            break;
        case EstadoServidor.PROP_GRUPOS_EXISTENTES:
            notificacion = eventoPorCambioEnGrupos(evento);
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
            
            eventoServidor.getReceptores().forEach(receptor -> {

                try 
                {
                    Socket cliente = receptor.getSocket();
                    int idUsuario = receptor.getId();
                    
                    // Usar el id de cada cliente al enviarle su notificación.
                    eventoServidor.setIdUsuarioCliente(idUsuario);
                    
                    ManejadorCliente.enviarEventoASocket(cliente, eventoServidor);
                } 
                catch (IOException e) 
                {
                    queueEventos.offer(new Evento(TipoDeEvento.ERROR_SERVIDOR, e.getMessage()));
                }
            });
        }
    }
    
    private EventoServidor eventoPorUsuariosConectados(PropertyChangeEvent evento)
    {
        EventoServidor notificacion;

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
        
        final Collection<Cliente> todosLosConectados = estadoServidor
                .getClientesConectados().values();
                
        // Un cambio en los usuarios conectados es enviado a todos los demas
        // usuarios que esten conectados.
        notificacion.agregarReceptores((Set<Cliente>) todosLosConectados);
        
        return notificacion;
    }
    
    /**
     * Determina el tipo de evento producido por un cambio en las invitaciones y
     * produce el EventoServidor correspondiente, con los receptores necesarios.
     * 
     * @param evento El evento de cambio en las invitaciones.
     * @return El EventoServidor adecuado.
     */
    private EventoServidor eventoPorCambioEnInvitacion(
        PropertyChangeEvent evento
    ) 
    {
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
                final int idUsuarioQueInvita = invitacion.getIdUsuarioQueInvita();
                final Cliente clienteQueInvito = estadoServidor.getClientePorId(idUsuarioQueInvita);
                            
                if (clienteQueInvito != null)
                {
                    notificacion = new EventoServidor(
                        TipoDeEvento.AMISTAD_ACEPTADA,
                        jsonOtroUsuario
                    );
                    
                    // El usuario tiene un nuevo amigo. Notificarle a ese usuario con 
                    // los datos de su nuevo amigo.
                    notificacion.agregarReceptor(clienteQueInvito);
                }
            }
            else 
            {
                DTOGrupo grupo = estadoServidor.getGrupoPorId(invitacion.getIdGrupo());
                
                // Enviar la notificacion si el grupo existe.
                if (grupo != null)
                {
                    notificacion = new EventoServidor(
                        TipoDeEvento.USUARIO_SE_UNIO_A_GRUPO,
                        jsonOtroUsuario
                    );
                    
                    // Agregar como receptores a todos los usuarios conectados 
                    // que forman parte del grupo.
                    for (Integer idMiembro : grupo.getIdsUsuariosMiembro())
                    {
                        final Cliente clienteMiembro = estadoServidor.getClientePorId(idMiembro);
                        
                        if (clienteMiembro != null) 
                        {
                            notificacion.agregarReceptor(clienteMiembro);
                        }
                    }
                }
            }
        }
        else if (evento.getNewValue() instanceof DTOInvitacion && evento.getOldValue() == null)
        {
            final DTOInvitacion invitacion = (DTOInvitacion) evento.getNewValue();
            
            // La invitación fue enviada, notificar al usuario invitado.
            final Cliente clienteInvitado = estadoServidor
                    .getClientePorId(invitacion.getIdUsuarioInvitado());
            
            if (clienteInvitado != null)
            {
                final String jsonInvitacion = gson.toJson(invitacion);
                
                notificacion = new EventoServidor(
                    TipoDeEvento.INVITACION_ENVIADA,
                    jsonInvitacion
                );
                
                notificacion.agregarReceptor(clienteInvitado);
            }
        }
        else if (evento.getNewValue() == null && evento.getOldValue() instanceof DTOInvitacion)
        {
            //TODO: Revisar si es necesario este evento.
            final DTOInvitacion invRechazada = (DTOInvitacion) evento.getOldValue();
            
            // La invitación fue enviada, notificar al usuario invitado.
            final Cliente clienteQueInvita = estadoServidor
                    .getClientePorId(invRechazada.getIdUsuarioQueInvita());
            
            // Entregar evento del servidor solo al cliente que envió la notificación, 
            // si es que está conectado.
            if (clienteQueInvita != null)
            {
                final DTOIdEntidad idInvRechazada = new DTOIdEntidad(invRechazada.getId());
            
                final String jsonInvRechazada = gson.toJson(idInvRechazada);

                // La invitación fue rechazada.
                notificacion = new EventoServidor(
                    TipoDeEvento.AMISTAD_RECHAZADA,
                    jsonInvRechazada
                );
                
                notificacion.agregarReceptor(clienteQueInvita);
            }
        }
        
        return notificacion;
    }
    
    private EventoServidor eventoPorMensajeRecibido(PropertyChangeEvent evento) 
    {
        EventoServidor notificacion = null;
        
        if (evento.getNewValue() instanceof DTOMensaje)
        {
            final DTOMensaje mensaje = (DTOMensaje) evento.getNewValue();

            final Cliente clienteDest = estadoServidor.getClientePorId(mensaje.getIdDestinatario());

            // Solo enviar el evento si el cliente del destinatario esta conectado.
            if (clienteDest != null)
            {
                final String jsonMensajeRecibido = gson.toJson(mensaje);

                notificacion = new EventoServidor(
                    TipoDeEvento.MENSAJE_ENVIADO, 
                    jsonMensajeRecibido
                );
                
                // Si el mensaje fue agregado para este usuario, notificar con 
                // los datos del nuevo mensaje.
                notificacion.agregarReceptor(clienteDest);
            }
        }
        
        return notificacion;
    }
    
    private EventoServidor eventoPorCambioEnGrupos(PropertyChangeEvent evento)
    {       
        EventoServidor notificacion;
        
        if (evento.getOldValue() instanceof Integer && evento.getNewValue() == null)
        {
            // El grupo fue eliminado.
            final int idGrupoEliminado = (int) evento.getOldValue();

            DTOGrupo grupo = estadoServidor.getGrupoPorId(idGrupoEliminado);

            if (grupo != null)
            {
                DTOIdEntidad objIdGrupo = new DTOIdEntidad(grupo.getId());

                String jsonIdGrupoElim = gson.toJson(objIdGrupo);

                notificacion = new EventoServidor(
                    TipoDeEvento.GRUPO_ELIMINADO, 
                    jsonIdGrupoElim
                );
                
                grupo.getIdsUsuariosMiembro().forEach(idMiembro -> {
                    
                    final Cliente clienteMiembro = estadoServidor.getClientePorId(idMiembro);
                    
                    if (clienteMiembro != null) 
                    {
                        // Agregar como receptores a todos los usuarios conectados 
                        // que forman parte del grupo.
                        notificacion.agregarReceptor(clienteMiembro);
                    }
                });
            }
        }
        else if (evento.getOldValue() instanceof DTOAbandonarGrupo && evento.getNewValue() == null)
        {
            // Un usuario abandono el grupo.
            final DTOAbandonarGrupo abandono = (DTOAbandonarGrupo) evento.getOldValue();
            
            final DTOIdEntidad idUsuario = new DTOIdEntidad(abandono.getIdUsuarioQueAbandono());

            final String idUsuarioJson = gson.toJson(idUsuario);

            notificacion = new EventoServidor(
                TipoDeEvento.USUARIO_ABANDONO_GRUPO, 
                idUsuarioJson
            );
            
            // Enviar evento del servidor a cada miembro del grupo que este conectado.
            abandono.getGrupoAbandonado().getIdsUsuariosMiembro().forEach(idMiembro -> {

                final Cliente clienteMiembro = estadoServidor.getClientePorId(idMiembro);

                if (clienteMiembro != null) 
                {
                    // Agregar como receptores a todos los usuarios conectados 
                    // que forman parte del grupo.
                    notificacion.agregarReceptor(clienteMiembro);
                }
            });
            
            return notificacion;
        }
        
        return null;
    }
}
