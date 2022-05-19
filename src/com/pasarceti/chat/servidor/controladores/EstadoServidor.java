package com.pasarceti.chat.servidor.controladores;

import com.pasarceti.chat.servidor.modelos.Amistad;
import com.pasarceti.chat.servidor.modelos.AmistadDAO;
import com.pasarceti.chat.servidor.modelos.Cliente;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.pasarceti.chat.servidor.modelos.TipoDestinatario;
import com.pasarceti.chat.servidor.modelos.UsuariosGrupo;
import com.pasarceti.chat.servidor.modelos.UsuariosGrupoDAO;
import com.pasarceti.chat.servidor.modelos.dto.DTOAbandonarGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOContactos;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvAceptada;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;

/**
 * @brief Almacena todo el estado de los datos del servidor y notifica a los 
 * clientes cuando se produce un cambio en alguna de sus propiedades.
 */
public class EstadoServidor 
{
    public static final String PROP_USUARIOS_CONECTADOS = "USUARIOS_CONECTADOS";
    public static final String PROP_USUARIO_ACTUAL = "USUARIO_ACTUAL";

    public static final String PROP_TODOS_USUARIOS = "USUARIOS";
    public static final String PROP_GRUPOS_EXISTENTES = "GRUPOS";
    
    public static final String PROP_INVITACIONES_RECIBIDAS = "INVITACIONES_RECIBIDAS";
    public static final String PROP_MENSAJES_RECIBIDOS = "MENSAJES_RECIBIDOS";

    private final PropertyChangeSupport soporteCambios = new PropertyChangeSupport(this);

    // Un mapa concurrente con <idUsuario,  socket> de los clientes conectados. 
    // Recordar que es más eficiente en iteración que en modificación.
    // Cuando un usuario está conectado, pero no ha iniciado sesió
    private final ConcurrentHashMap<Integer, Cliente> clientesConectados = new ConcurrentHashMap<>();

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
        usuarios.forEach(usuario -> {
            invitaciones.putIfAbsent(usuario.getIdUsuario(), new ArrayList<>());
            mensajesEnviados.putIfAbsent(usuario.getIdUsuario(), new ArrayList<>());
        });
    }
    
    /**
     * Registra un listener suscrito a los cambios en todas las propiedades 
     * de estado del servidor.
     * 
     * @param listener El listener a agregar.
     */
    public void agregarListener(PropertyChangeListener listener)
    {
        soporteCambios.addPropertyChangeListener(listener);
    }
    
    /**
     * Agrega un listener suscrito a los cambios en una propiedad específica del
     * estado del servidor.
     * 
     * @param propiedad La propiedad a la que va a escuchar el listener.
     * @param listener El listener a agregar.
     */
    public void agregarListener(String propiedad, PropertyChangeListener listener)
    {
        if (listener == null) {
            return;
        }
        
        switch (propiedad)
        {
        case PROP_USUARIOS_CONECTADOS:
        case PROP_TODOS_USUARIOS:
        case PROP_GRUPOS_EXISTENTES:
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
     * @param cliente El cliente a agregar.
    */
    public void agregarCliente(Cliente cliente)
    {
        clientesConectados.putIfAbsent(cliente.getId(), cliente);
        
        invitaciones.putIfAbsent(cliente.getId(), new ArrayList<>());
        mensajesEnviados.putIfAbsent(cliente.getId(), new ArrayList<>());
        
        DTOUsuario usuario = getUsuarioPorId(cliente.getId());

        soporteCambios.fireIndexedPropertyChange(
            PROP_USUARIOS_CONECTADOS, 
            usuario.getIdUsuario(), 
            null, 
            usuario
        );
    }

    /**
     * @brief Remueve un cliente de los usuarios conectados, si existe, y notifica
     * a todos los listeners de PROP_USUARIOS_CONECTADOS.
     * 
     * @param idCliente El ID del cliente a remover.
    */
    public void removerCliente(Integer idCliente)
    {
        Cliente clientePrevio = clientesConectados.remove(idCliente);
        
        if (clientePrevio != null) 
        {
            soporteCambios.firePropertyChange(PROP_USUARIOS_CONECTADOS, idCliente, null);
        }
    }

    /**
    * Remueve al usuario actual de la lista de usuarios conectados y, si se remueve
    * con éxito, reinicia el valor de idUsuario a -1 y notifica a los listeners.
    * 
     * @param idUsuario El ID del usuario que se va a desconectar.
    */
    public void desconectarUsuario(ThreadLocal<Integer> idUsuario) 
    {
        int viejoIdUsuario = idUsuario.get();
        Cliente clientePrevio = clientesConectados.remove(viejoIdUsuario);
        
        invitaciones.putIfAbsent(idUsuario.get(), new ArrayList<>());
        mensajesEnviados.putIfAbsent(idUsuario.get(), new ArrayList<>());

        if (clientePrevio != null) 
        {
            idUsuario.set(-1);

            soporteCambios.fireIndexedPropertyChange(PROP_USUARIOS_CONECTADOS, viejoIdUsuario, clientePrevio, null);
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
    public void enviarMensaje(DTOMensaje mensajeEnviado)
    {
        List<Integer> idsDestinatarios = new ArrayList<>();
        
        if (mensajeEnviado.getTipoDestinatario().equals(TipoDestinatario.GRUPO)) 
        {
            DTOGrupo grupoDestinatario = getGrupoPorId(mensajeEnviado.getIdDestinatario());
            
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
     * @param idDestinatario El ID del usuario que va a recibir este mensaje.
     * @param mensajeEnviado El mensaje a enviar.
    */
    public void enviarMensajeAUsuario(int idDestinatario, DTOMensaje mensajeEnviado)
    {
        // Agregar el mensaje enviado a la lista de mensajes recibidos del
        // destinatario del mensaje. Se agrega en la primera posición, de esta 
        // forma ordenando mensajes más recientes antes.
        List<DTOMensaje> mensajesDelDestinatario = mensajesEnviados.get(idDestinatario);
        DTOMensaje mensajeAnterior = mensajesDelDestinatario.isEmpty()
                ? null
                : mensajesDelDestinatario.get(0);
        
        mensajeEnviado.setIdDestinatario(idDestinatario);
        
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
    public void enviarInvitacion(DTOInvitacion invitacion)
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
    
    public void invitacionAceptada(DTOInvAceptada invitacionAceptada)
    {
        soporteCambios.firePropertyChange(
            PROP_INVITACIONES_RECIBIDAS,
            invitacionAceptada,
            null
        );
    }
    
    public void invitacionRechazada(DTOInvitacion invitacion)
    {
        soporteCambios.firePropertyChange(
            PROP_INVITACIONES_RECIBIDAS,
            invitacion,
            null
        );
    }
    
    public void usuarioAbandono(DTOAbandonarGrupo grupoAbandonado)
    {
        soporteCambios.firePropertyChange(
            PROP_GRUPOS_EXISTENTES,
            grupoAbandonado,
            null
        );
    }
    
    public void grupoEliminado(int idGrupo)
    {
        soporteCambios.firePropertyChange(
            PROP_GRUPOS_EXISTENTES,
            (Integer) idGrupo,
            null
        );
    }

    /**
     * @brief Remueve una invitación a un grupo y notifica a todos los listeners 
     * de PROP_TODAS_INVITACIONES.
     * 
     * @param idGrupo El ID del grupo al que fue invitado el usuario.
     * @param idUsuario El ID del usuario a quien fue dirigida la invitación a remover.
    */
    public void removerInvitacion(int idGrupo, int idUsuario)
    {
        DTOInvitacion invPorRemover = null;
        int idxInvitacionPorRemover = -1;
        int i = 0;
        
        List<DTOInvitacion> invitacionesUsuario = invitaciones.get(idUsuario);

        for (DTOInvitacion invitacion : invitacionesUsuario)
        {
            if (invitacion.getIdGrupo() == idGrupo) 
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

        boolean removida = invitacionesUsuario.remove(invPorRemover);

        if (removida) 
        {
            soporteCambios.fireIndexedPropertyChange(
                PROP_INVITACIONES_RECIBIDAS, 
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
     * @param idUsuario El ID del usuario que va a obtener sus contactos.
     * @param amistades La coleccion de amistades que tiene el usaurio.
     * @param usuariosGrupo Los grupos de los que forma parte el usuario.
     * @return El objeto con las listas de contactos del usuario.
    */
    public DTOContactos getContactosUsuario(int idUsuario, List<Amistad> amistades, List<UsuariosGrupo> usuariosGrupo) 
    {
        List<DTOUsuario> amigos = getAmigosDeUsuario(idUsuario, amistades);

        List<DTOUsuario> usuariosConectados = getUsuariosConectados(idUsuario, amigos);

        List<DTOGrupo> grupos = getGruposDeUsuario(idUsuario, usuariosGrupo);
        
        return new DTOContactos(
            amigos,
            usuariosConectados,
            grupos,
            new ArrayList<>()
        );
    }
    
     public List<DTOUsuario> getUsuariosConectados(int idUsuarioActual, List<DTOUsuario> amigos)
    {
        List<DTOUsuario> usuariosConectados = new ArrayList<>();

        clientesConectados.forEachKey(100, (Integer idUsuario) -> {
            if (idUsuario != idUsuarioActual)
            {
                DTOUsuario usuario = getUsuarioPorId(idUsuario);
                if (usuario != null && !amigos.contains(usuario))
                {
                    usuariosConectados.add(usuario);
                }
            }
        });

        return usuariosConectados;
    }
    
    public DTOUsuario getUsuarioPorId(int idUsuario) 
    {
        for (DTOUsuario usuario : usuarios) 
        {
            if (usuario.getIdUsuario() == idUsuario) 
            {
                return usuario;
            }
        }
        
        return null;
    }
    
    /**
     * @brief Busca todas las invitaciones pendientes de un usuario en 
     * especifico.
     * 
     * @param idUsuario El ID del usuario a remover.
     * @return La coleccion con todas las invitaciones pendientes.
    */
    public List<DTOInvitacion> getInvitacionesUsuario(int idUsuario)
    {
        return invitaciones.get(idUsuario);
    }
    
    public List<DTOUsuario> getAmigosDeUsuario(int idUsuario, List<Amistad> amistades)
    {
        List<DTOUsuario> amigos = new ArrayList<>();

        amistades.forEach((amistad) -> {
            DTOUsuario usuarioAmigo = getUsuarioPorId(idUsuario);

            if (usuarioAmigo != null)
            {
                amigos.add(usuarioAmigo);
            }
        });

        return amigos;
    }
    
    public Cliente getClientePorId(Integer idCliente)
    {
        return clientesConectados.get(idCliente);
    }

    public List<DTOMensaje> getMensajesUsuario(int idUsuario)
    {
        final List<DTOMensaje> mensajes = mensajesEnviados.get(idUsuario);

        return (mensajes != null) ? mensajes : new ArrayList<>();
    }

    // Getters para cada una de las colecciones del estado.
    public ConcurrentHashMap<Integer, Cliente> getClientesConectados() 
    {
        return clientesConectados;
    }

    public CopyOnWriteArraySet<DTOUsuario> getUsuarios() {
        return usuarios;
    }

    public ConcurrentHashMap<Integer, List<DTOInvitacion>> getInvitaciones() {
        return invitaciones;
    }

    public CopyOnWriteArraySet<DTOGrupo> getGrupos() {
        return grupos;
    }
    
    public DTOGrupo getGrupoPorId(int idGrupo)
    {
        DTOGrupo grupoConId = null;
                    
        for (DTOGrupo grupo : grupos)
        {
            if (grupo.getId() == idGrupo)
            {
                grupoConId = grupo;
                break;
            }
        }
        
        return grupoConId;
    }
    
    public List<DTOGrupo> getGruposDeUsuario(int idUsuario, List<UsuariosGrupo> usuariosGrupo)
    {
        List<DTOGrupo> grupos = new ArrayList<>();

        usuariosGrupo.forEach((usuarioGrupo) ->{
            DTOGrupo grupoConId = getGrupoPorId(usuarioGrupo.getId_grupo());            
            if (grupoConId != null)
            {
                grupos.add(grupoConId);
            }
        });

        return grupos;
    }

    public ConcurrentHashMap<Integer, List<DTOMensaje>> getMensajesRecibidos() {
        return mensajesEnviados;
    }
}
