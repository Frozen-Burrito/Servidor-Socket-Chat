package com.pasarceti.chat.servidor.controladores;

import com.pasarceti.chat.servidor.modelos.TipoDestinatario;
import com.pasarceti.chat.servidor.modelos.dto.DTODestinatario;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTONuevoMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @brief Almacena todo el estado de los datos del servidor y notifica a los 
 * clientes cuando se produce un cambio en alguna de sus propiedades.
 */
public class EstadoServidor 
{
    public static final String PROP_USUARIOS_CONECTADOS = "USUARIOS_CONECTADOS";
    public static final String PROP_USUARIO_ACTUAL = "USUARIO_ACTUAL";

    public static final String PROP_TODOS_USUARIOS = "USUARIOS";
    public static final String PROP_TODOS_GRUPOS = "GRUPOS";
    
    public static final String PROP_INVITACIONES_RECIBIDAS = "INVITACIONES_RECIBIDAS";
    public static final String PROP_MENSAJES_RECIBIDOS = "MENSAJES_RECIBIDOS";

    private final PropertyChangeSupport soporteCambios = new PropertyChangeSupport(this);

    // Un mapa concurrente con <idUsuario,  socket> de los clientes conectados. 
    // Recordar que es más eficiente en iteración que en modificación.
    // Cuando un usuario está conectado, pero no ha iniciado sesió
    private final ConcurrentHashMap<Integer, Socket> clientesConectados = new ConcurrentHashMap<>();

    // Las listas con todos los registros actuales del servidor.
    private final CopyOnWriteArraySet<DTOUsuario> usuarios = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<DTOGrupo> grupos = new CopyOnWriteArraySet<>();
    
    // Un mapa concurrente con <idUsuario, Lista<Invitacion>> que almacena las
    // invitaciones pendientes de cada usuario.
    private final ConcurrentHashMap<Integer, List<DTOInvitacion>> invitaciones = new ConcurrentHashMap<>();

    // Un mapa concurrente con <idUsuario, Lista<Mensajes>> que almacena los
    // mensajes recibidos por los usuarios.
    private final ConcurrentHashMap<Integer, List<DTOMensaje>> mensajesEnviados = new ConcurrentHashMap<>();

    public EstadoServidor() 
    {
        // Inicializar todas las listas de entidades para cada uno de
        // los usuarios del sistema.
        
        // PROBLEMA: Esto solo maneja los usuarios existentes cuando se inicia
        // el servidor, no los nuevos usuarios registrados mientras se ejecuta.
        usuarios.forEach(usuario -> {
            invitaciones.putIfAbsent(usuario.getIdUsuario(), new ArrayList<>());
            mensajesEnviados.putIfAbsent(usuario.getIdUsuario(), new ArrayList<>());
        });
    }

    public void agregarListener(String propiedad, PropertyChangeListener listener)
    {
        if (listener == null) {
            return;
        }
        
        switch (propiedad)
        {
        case PROP_USUARIOS_CONECTADOS:
        case PROP_TODOS_USUARIOS:
        case PROP_TODOS_GRUPOS:
        case PROP_INVITACIONES_RECIBIDAS:
        case PROP_MENSAJES_RECIBIDOS:
            // Agregar un listener para la propiedad requerida.
            soporteCambios.addPropertyChangeListener(propiedad, listener);
            break;

        default: throw new IllegalArgumentException("Propiedad de estado no soportada.");
        }
    }

    public void removerListener(PropertyChangeListener listener)
    {
        soporteCambios.removePropertyChangeListener(listener);
    }

    /**
     * @brief Agrega un nuevo cliente, si no existe antes, y notifica a todos los
     * listeners de PROP_USUARIOS_CONECTADOS.
     * 
     * @param idUsuario El ID del usuario conectado.
     * @param socket El socket para la conexión del usuario.
    */
    public synchronized void agregarCliente(int idUsuario, Socket socket)
    {
        Socket clientePrevio = clientesConectados.putIfAbsent(idUsuario, socket);

        soporteCambios.fireIndexedPropertyChange(PROP_USUARIOS_CONECTADOS, idUsuario, clientePrevio, socket);
    }

    /**
     * @brief Remueve un cliente de los usuarios conectados, si existe, y notifica
     * a todos los listeners de PROP_USUARIOS_CONECTADOS.
     * 
     * @param idUsuario El ID del usuario a remover.
    */
    public synchronized void removerCliente(int idUsuario)
    {
        Socket clientePrevio = clientesConectados.remove(idUsuario);

        soporteCambios.fireIndexedPropertyChange(PROP_USUARIOS_CONECTADOS, idUsuario, clientePrevio, null);
    }

    /**
    * Remueve al usuario actual de la lista de usuarios conectados y, si se remueve
    * con éxito, reinicia el valor de idUsuario a -1 y notifica a los listeners.
    * 
     * @param idUsuario El ID del usuario que se va a desconectar.
    */
    public synchronized void desconectarUsuario(ThreadLocal<Integer> idUsuario) 
    {
        int viejoIdUsuario = idUsuario.get();
        Socket clientePrevio = clientesConectados.remove(viejoIdUsuario);

        if (clientePrevio != null) 
        {
            idUsuario.set(-1);

            soporteCambios.fireIndexedPropertyChange(PROP_USUARIOS_CONECTADOS, viejoIdUsuario, clientePrevio, null);
            soporteCambios.firePropertyChange(PROP_USUARIOS_CONECTADOS, viejoIdUsuario, (int) idUsuario.get());
        }
    }
    
    /**
     * @brief Envía un nuevo mensaje y notifica al cliente de destinatario del
     * mensaje, que debe estar agregado como listener de MENSAJES_RECIBIDOS.
     * 
     * Si el tipo de destinatario es un grupo, el mensaje es enviado a cada uno 
     * de los miembros de ese grupo.
     * 
     * @param mensajeEnviado El mensaje a enviar.
    */
    public synchronized void enviarMensaje(DTOMensaje mensajeEnviado)
    {
        List<Integer> idsDestinatarios = new ArrayList<>();
        
        if (mensajeEnviado.getTipoDestinatario() == TipoDestinatario.GRUPO) 
        {
            DTOGrupo grupoDestinatario = null;
            
            for (DTOGrupo grupo : grupos)
            {
                if (grupo.getId() == mensajeEnviado.getIdDestinatario())
                {
                    grupoDestinatario = grupo;
                    break;
                }
            }
            
            if (grupoDestinatario != null)
            {
                idsDestinatarios.addAll(grupoDestinatario.getIdsUsuariosMiembro());
            }
        }
        else 
        {
            idsDestinatarios.add(mensajeEnviado.getIdDestinatario());
        }
        
        idsDestinatarios.forEach(destinatario -> {
            enviarMensajeAUsuario(destinatario, mensajeEnviado);
        });
    }
    
    /**
     * @brief Envía un nuevo mensaje y notifica al cliente de destinatario del
     * mensaje, que debe estar agregado como listener de MENSAJES_RECIBIDOS.
     * 
     * @param destinatario El ID del usuario que va a recibir este mensaje.
     * @param mensajeEnviado El mensaje a enviar.
    */
    public synchronized void enviarMensajeAUsuario(int destinatario, DTOMensaje mensajeEnviado)
    {
        // Agregar el mensaje enviado a la lista de mensajes recibidos del
        // destinatario del mensaje. Se agrega en la primera posición, de esta 
        // forma ordenando mensajes más recientes antes.
        List<DTOMensaje> mensajesDelDestinatario = mensajesEnviados.get(destinatario);
        DTOMensaje mensajeAnterior = mensajesDelDestinatario.isEmpty()
                ? null
                : mensajesDelDestinatario.get(0);
        
        mensajesDelDestinatario.add(0, mensajeEnviado);

        soporteCambios.fireIndexedPropertyChange(PROP_MENSAJES_RECIBIDOS, mensajeEnviado.getIdDestinatario(), mensajeAnterior, mensajeEnviado);
    }
    
    /**
     * @brief Agrega una nueva invitación a la lista de invitaciones pendientes 
     * del usuario invitado. Luego, notifica usuario invitado sobre la invitación
     * recibida. 
     * 
     * Para recibir este evento, debe estar agregado como listener de 
     * PROP_INVITACIONES_RECIBIDAS.
     * 
     * @param invitacion La invitación enviada al otro usuario.
     */
    public synchronized void enviarInvitacion(DTOInvitacion invitacion)
    {
        List<DTOInvitacion> invitacionesPendientes = invitaciones.get(invitacion.getIdUsuarioInvitado());
        
        // Obtener el número de invitaciones antes de agregar la nueva.
        int longitudAnterior = invitacionesPendientes.size();
        // add() retorna true si la colección fue modificada.
        boolean invModificadas = invitacionesPendientes.add(invitacion);
        
        if (invModificadas && longitudAnterior == invitacionesPendientes.size() -1)
        {
            // Si hubo un cambio, notificar a los listeners.
            soporteCambios.fireIndexedPropertyChange(
                PROP_INVITACIONES_RECIBIDAS, 
                longitudAnterior, 
                null, 
                invitacion
            );
        }
    }

    /**
     * @brief Remueve una invitación a un grupo y notifica a todos los listeners 
     * de PROP_TODAS_INVITACIONES.
     * 
     * @param idGrupo El ID del grupo al que fue invitado el usuario.
     * @param idUsuario El ID del usuario a quien fue dirigida la invitación a remover.
    */
    public synchronized void removerInvitacion(int idGrupo, int idUsuario)
    {
        DTOInvitacion invPorRemover = null;
        int idxInvitacionPorRemover = -1;
        int i = 0;

        for (DTOInvitacion invitacion : invitaciones)
        {
            if (invitacion.getIdGrupo() == idGrupo && invitacion.getIdUsuarioInvitado() == idUsuario) 
            {
                invPorRemover = invitacion;
                idxInvitacionPorRemover = i;
                break;
            }
            ++i;
        }

        if (invPorRemover == null || idxInvitacionPorRemover < 0) 
        {
            return;
        } 

        boolean removida = invitaciones.remove(invPorRemover);

        if (removida) 
        {
            soporteCambios.fireIndexedPropertyChange(
                PROP_TODAS_INVITACIONES, 
                idxInvitacionPorRemover, 
                invPorRemover, 
                null
            );
        }
    }

    /**
     * @brief Busca todos los contactos de un usuario y los retorna en una sola
     * lista de posibles Destinatarios (usuarios, amigos y grupos).
     * 
     * @param idUsuario El ID del usuario a remover.
    */
    public synchronized void getContactosUsuario(int idUsuario) 
    {
        final List<DTODestinatario> contactos = new ArrayList<>();

//        for (DTOUsuario)
    }

    /**
     * @brief Busca todas las invitaciones pendientes de un usuario en 
     * especifico.
     * 
     * @param idUsuario El ID del usuario a remover.
     * @return La coleccion con todas las invitaciones pendientes.
    */
    public synchronized List<DTOInvitacion> getInvitacionesUsuario(int idUsuario)
    {
        final List<DTOInvitacion> invitacionesUsuario = new ArrayList<>();

        for (DTOInvitacion invitacion : invitaciones)
        {
            if (invitacion.getIdUsuarioInvitado() == idUsuario)
            {
                invitacionesUsuario.add(invitacion);
            }
        }

        return invitacionesUsuario;
    }

    public List<DTOMensaje> getMensajesUsuario(int idUsuario)
    {
        final List<DTOMensaje> mensajes = mensajesEnviados.get(idUsuario);

        return (mensajes != null) ? mensajes : new ArrayList<>();
    }

    // Getters para cada una de las colecciones del estado.
    public ConcurrentHashMap<Integer, Socket> getClientesConectados() 
    {
        return clientesConectados;
    }

    public CopyOnWriteArraySet<DTOUsuario> getUsuarios() {
        return usuarios;
    }

    public CopyOnWriteArraySet<DTOInvitacion> getInvitaciones() {
        return invitaciones;
    }

    public CopyOnWriteArraySet<DTOGrupo> getGrupos() {
        return grupos;
    }

    public ConcurrentHashMap<Integer, List<DTOMensaje>> getMensajesRecibidos() {
        return mensajesEnviados;
    }
}
