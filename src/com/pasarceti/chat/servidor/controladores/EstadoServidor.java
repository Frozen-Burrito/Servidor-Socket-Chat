/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.pasarceti.chat.servidor.controladores;

import com.pasarceti.chat.servidor.modelos.dto.DTODestinatario;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 */
public class EstadoServidor 
{
    public static final String PROP_USUARIOS_CONECTADOS = "USUARIOS_CONECTADOS";
    public static final String PROP_USUARIO_ACTUAL = "USUARIO_ACTUAL";

    public static final String PROP_TODOS_USUARIOS = "USUARIOS";
    public static final String PROP_TODAS_INVITACIONES = "INVITACIONES";
    public static final String PROP_TODOS_GRUPOS = "GRUPOS";
    public static final String PROP_MENSAJES_RECIBIDOS = "MENSAJES_RECIBIDOS";

    private final PropertyChangeSupport soporteCambios = new PropertyChangeSupport(this);

    // Un mapa concurrente con <idUsuario,  socket> de los clientes conectados. 
    // Recordar que es más eficiente en iteración que en modificación.
    // Cuando un usuario está conectado, pero no ha iniciado sesió
    private final ConcurrentHashMap<Integer, Socket> clientesConectados = new ConcurrentHashMap<>();  

    // El ID del usuario conectado, es -1 si no ha iniciado sesion.
    private final AtomicInteger idUsuario = new AtomicInteger(-1);

    // Las listas con todos los registros actuales del servidor.
    private final CopyOnWriteArraySet<DTOUsuario> usuarios = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<DTOInvitacion> invitaciones = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<DTOGrupo> grupos = new CopyOnWriteArraySet<>();

    // Un mapa concurrente con <idUsuario, Lista<Mensajes>> que almacena los
    // mensajes recibidos por los usuarios.
    private final ConcurrentHashMap<Integer, List<DTOMensaje>> mensajesRecibidos = new ConcurrentHashMap<>();

    public EstadoServidor() 
    {

    }

//    public EstadoServidor(
//        CopyOnWriteArraySet<DTOUsuario> usuarios, 
//        CopyOnWriteArraySet<DTOInvitacion> invitaciones, 
//        CopyOnWriteArraySet<DTOGrupo> grupos
//    ) 
//    {
//        this.usuarios.addAll(usuarios);
//    }

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
        case PROP_TODAS_INVITACIONES:
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
    */
    public synchronized void desconectarUsuarioActual() 
    {
        Socket clientePrevio = clientesConectados.remove(idUsuario.get());

        if (clientePrevio != null) 
        {
            int viejoIdUsuario = idUsuario.getAndSet(-1);

            soporteCambios.fireIndexedPropertyChange(PROP_USUARIOS_CONECTADOS, viejoIdUsuario, clientePrevio, null);
            soporteCambios.firePropertyChange(PROP_USUARIOS_CONECTADOS, viejoIdUsuario, idUsuario.get());
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
     * @brief Busca todas las invitaciones donde su idUsuarioInvitado sea 
     * idéntico a idUsuario.
     * 
     * @param idUsuario El ID del usuario a remover.
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
        final List<DTOMensaje> mensajes = mensajesRecibidos.get(idUsuario);

        return (mensajes != null) ? mensajes : new ArrayList<>();
    }

    public List<DTOMensaje> getMensajesUsuarioActual()
    {
        final List<DTOMensaje> mensajes = mensajesRecibidos.get(idUsuario.get());

        return (mensajes != null) ? mensajes : new ArrayList<>();
    }

    // Getters para cada una de las colecciones del estado.
    public ConcurrentHashMap<Integer, Socket> getClientesConectados() 
    {
        return clientesConectados;
    }

    /**
     * Retorna el ID del usuario actual, que ha iniciado sesión.
     * Si el cliente no ha iniciado sesión, esto retorna -1.
     */
    public synchronized int getIdUsuarioActual() 
    {
        return idUsuario.get();
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
        return mensajesRecibidos;
    }
}
