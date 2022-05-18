package com.pasarceti.chat.servidor.controladores;

import java.net.Socket;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pasarceti.chat.servidor.modelos.AccionCliente;
import com.pasarceti.chat.servidor.modelos.Amistad;
import com.pasarceti.chat.servidor.modelos.AmistadDAO;
import com.pasarceti.chat.servidor.modelos.Cliente;
import com.pasarceti.chat.servidor.modelos.ErrorEvento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.Grupo;
import com.pasarceti.chat.servidor.modelos.GrupoDAO;
import com.pasarceti.chat.servidor.modelos.Invitacion;
import com.pasarceti.chat.servidor.modelos.InvitacionDAO;
import com.pasarceti.chat.servidor.modelos.Mensaje;
import com.pasarceti.chat.servidor.modelos.MensajeDAO;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.TipoDestinatario;
import com.pasarceti.chat.servidor.modelos.Usuario;
import com.pasarceti.chat.servidor.modelos.UsuarioDAO;
import com.pasarceti.chat.servidor.modelos.UsuariosGrupo;
import com.pasarceti.chat.servidor.modelos.UsuariosGrupoDAO;
import com.pasarceti.chat.servidor.modelos.dto.DTOAbandonarGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOCambioPassword;
import com.pasarceti.chat.servidor.modelos.dto.DTOContacto;
import com.pasarceti.chat.servidor.modelos.dto.DTOContactos;
import com.pasarceti.chat.servidor.modelos.dto.DTOCredUsuario;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOIdEntidad;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvAceptada;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOListaMensajes;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTONuevoMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTONuevoGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Permite a los clientes realizar acciones con el chat, que producen un resultado.
 * 
 * Algunas acciones, como enviar un mensaje, producen también un cambio en el
 * estado del servidor. Estos cambios son propagados por el estado del servidor.
 */
public class ControladorChat 
{
    private static final String ERR_GENERAL = "Hubo un error procesando la petición del cliente.";
    private static final String ERR_FORMATO_JSON = "El JSON en el cuerpo de la petición no tiene el formato correcto.";
    private static final String ERR_USUARIO_NO_COINCIDE = "El ID de la petición y el ID del cliente autenticado no coinciden.";
    private static final String ERR_USR_EXISTE = "Ya existe un usuario con este nombre de usuario:";
    private static final String ERR_USR_NO_EXISTE = "No existe un usuario con este nombre de usuario:";
    private static final String ERR_USR_PASS_INCORRECTO = "La contraseña es incorrecta.";

    private static final String ERR_USR_O_GRUP_NO_EXISTEN = "El usuario o grupo no existen.";
    private static final String ERR_GRUPO_NO_EXISTE = "El grupo no existe";
    private static final String ERR_DEST_NO_EXISTE = "El destinatario del mensaje no fue encontrado";
    private static final String ERR_INV_NO_EXISTE = "La invitación para el usuario no existe.";
    private static final String ERR_GRUPO_USUARIOS_INSUF = "El grupo tiene menos usuarios que el mínimo (3) para mantenerlo";
    private static final String ERR_GRUPO_USR_NO_ES_MIEMBRO = "El usuario no es miembro del grupo.";

    private final EstadoServidor estadoServidor;
    
    private final ThreadLocal<Integer> idUsuario;
    
    private final Gson gson = new Gson();
    
    private final UsuarioDAO usuarioDAO;
    private final MensajeDAO mensajeDAO;
    private final GrupoDAO grupoDAO;
    private final InvitacionDAO invitacionDAO;
    private final UsuariosGrupoDAO usuariosGrupoDAO;

    public ControladorChat(EstadoServidor estadoServidor, ThreadLocal<Integer> idUsuario)
    {
        this.estadoServidor = estadoServidor;
        this.idUsuario = idUsuario;
        
        usuarioDAO = new UsuarioDAO();
        mensajeDAO = new MensajeDAO();
        grupoDAO = new GrupoDAO();
        invitacionDAO = new InvitacionDAO();
        usuariosGrupoDAO = new UsuariosGrupoDAO();
    }

    /**
     * Procesa una acción recibida de un cliente, determina si es correcta,
     * accede y/o modifica la BD y produce un resultado según la acción.
     *  
     * @param cliente El socket 
     * @param accionCliente La acción solicitada por el cliente.
     * @return Un evento del servidor con la respuesta a la acción
     */
    public EventoServidor ejecutarAccion(Socket cliente, AccionCliente accionCliente)
    {
        final int idUsuarioAccion = accionCliente.getIdUsuarioCliente();
        final String datosJson = accionCliente.getCuerpoJSON();
        
        if (!accionCliente.esAccionDeAutenticacion() && idUsuarioAccion != idUsuario.get()) 
        {
            // Si el ID de usuario en la accion del cliente y el ID asociado al 
            // cliente no coinciden, producir un error de autenticacion.
            return new EventoServidor(
                TipoDeEvento.ERROR_AUTENTICACION, 
                idUsuarioAccion, 
                ERR_USUARIO_NO_COINCIDE
            );
        }

        switch (accionCliente.getTipoDeAccion())
        {
            case REGISTRAR_USUARIO: return registrarUsuario(datosJson);
            case INICIAR_SESION: return iniciarSesion(cliente, datosJson);
            case CERRAR_SESION: return cerrarSesion();
            case RECUPERAR_CONTRASEÑA: return recuperarPassword(datosJson);
            case OBTENER_MENSAJES: return obtenerMensajes(datosJson);
            case ENVIAR_MENSAJE: return enviarMensaje(datosJson);
            case CREAR_GRUPO: return crearGrupo(datosJson);
            case ENVIAR_INVITACION: return enviarInvitacion(datosJson);
            case ACEPTAR_INVITACION: return rechazarInvitacion(datosJson);
            case RECHAZAR_INVITACION: return aceptarInvitacion(datosJson);
            case ABANDONAR_GRUPO: return abandonarGrupo(datosJson);
            
            default: return new EventoServidor(
                    TipoDeEvento.ERROR_SERVIDOR,  
                    idUsuario.get(), 
                    ERR_GENERAL
            );
        }
    }

    private EventoServidor registrarUsuario(String json)
    {
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR,
            idUsuario.get(),
            ""
        );
        
        try 
        {
            DTOCredUsuario datosRegistro = gson.fromJson(json, DTOCredUsuario.class);

            // Revisar si todavía no existe un usuario con el mismo nombre.
            Usuario usuario = usuarioDAO.busqueda_porNombre(datosRegistro.getNombreUsuario());
            
            boolean usuarioNoExiste = usuario == null;

            if (usuarioNoExiste) 
            {
                // El usuario no ha sido creado, registrar un nuevo usuario.
                int idNuevoUsuario = usuarioDAO.crear(new Usuario(
                    datosRegistro.getNombreUsuario(),
                    datosRegistro.getPassword()
                ));

                // Usuario creado, registro exitoso.
                resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                resultado.setIdUsuarioCliente(idNuevoUsuario);

            } else {
                // Ya existe un usuario con este nombre de usuario, retornar error.
                String jsonErr = gson.toJson(new ErrorEvento(ERR_USR_EXISTE + " " + datosRegistro.getNombreUsuario()));
                
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(jsonErr);
            }

        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            String jsonErr = gson.toJson(new ErrorEvento(ERR_FORMATO_JSON));
            
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(jsonErr);
        }
        
        return resultado;
    }

    public EventoServidor iniciarSesion(final Socket cliente, String json) 
    {
        // La respuesta que será enviada al cliente.
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR, 
            idUsuario.get(), 
            ""
        );
        
        try 
        {
            DTOCredUsuario credenciales = gson.fromJson(json, DTOCredUsuario.class);

            // Revisar si ya existe el usuario en la BD.
            Usuario usuario = usuarioDAO.busqueda_porNombre(credenciales.getNombreUsuario());
            
            if (usuario != null && usuario.getPassword() != null) 
            {
                // El usuario ya ha sido creado, comparar las contraseñas.
                boolean passCoinciden = usuario.getPassword().equals(credenciales.getPassword());

                if (passCoinciden)
                {
                    int idUsuarioDeBD = 0;

                    estadoServidor.agregarCliente(new Cliente(idUsuarioDeBD, cliente));
                    
                    //TODO: Obtener datos del usuario (Amigos, grupos, invitaciones).
                    DTOContactos listaDeContactos;
                    listaDeContactos = new DTOContactos(
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>()
                    );

                    String contactosJson = gson.toJson(listaDeContactos);
                    
                    // Usuario accedio con exito.                    
                    resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                    resultado.setCuerpoJSON(contactosJson);
                    
                } else {
                    // La contraseña es incorrecta, retornar error.
                    resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                    resultado.setCuerpoJSON(ERR_USR_PASS_INCORRECTO);
                }

            } else {
                // El usuario ya existe, retornar error.
                String jsonErr = gson.toJson(new ErrorEvento(ERR_USR_NO_EXISTE + " " + credenciales.getNombreUsuario()));
                
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(jsonErr);
            }

        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            String jsonErr = gson.toJson(new ErrorEvento(ERR_FORMATO_JSON));
            
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(jsonErr);
        }
        
        return resultado;
    }

    public EventoServidor cerrarSesion() 
    {
        // Actualizar estado con la desconexión del usuario actual.
        estadoServidor.desconectarUsuario(idUsuario);
        
        // Usuario cerró sesión.
        return new EventoServidor(
            TipoDeEvento.RESULTADO_OK,
            -1,
            ""
        );
    }

    public EventoServidor recuperarPassword(String json) 
    {
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR,
            idUsuario.get(),
            ""
        );
        
        try 
        {
            DTOCambioPassword solicitudCambio = gson.fromJson(json, DTOCambioPassword.class);

            // Asegurar que el usuario ya existe en la BD
            Usuario usuario = usuarioDAO.busqueda_porId(solicitudCambio.getIdUsuario());
            
            // Revisar si el usuario existe.
            if (usuario != null) 
            {
                // Actualizar registro de usuario con nueva contraseña en BD.
                usuario.setPassword(solicitudCambio.getNuevaPassword());
                
                usuarioDAO.actualizar(usuario);

                // Usuario creado, registro exitoso.
                resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);

            } else {
                // El usuario no existe, retornar error.
                String jsonErr = gson.toJson(new ErrorEvento(ERR_USR_NO_EXISTE));
                
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(jsonErr);
            }

        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            String jsonErr = gson.toJson(new ErrorEvento(ERR_FORMATO_JSON));
            
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(jsonErr);
        }
        
        return resultado;
    }
    
    public EventoServidor obtenerMensajes(String json) 
    {
        // La respuesta que será enviada al cliente.
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR, 
            idUsuario.get(), 
            ""
        );
        
        try 
        {
            DTOContacto contactoDeMensajes = gson.fromJson(json, DTOContacto.class);
            
            if (contactoDeMensajes.getTipoDestinatario() == TipoDestinatario.GRUPO)
            {
                Grupo grupo = grupoDAO.buscar_porId(contactoDeMensajes.getIdDestinatario());
                 
                if (grupo != null)
                {
                    // Obtener mensajes enviados al contacto.
                    List<Mensaje> mensajesGrupo = mensajeDAO.busqueda_porGrupo(grupo);
                    List<DTOMensaje> mensajes = new ArrayList<>();
                    
                    mensajesGrupo.forEach((mensaje) -> {
                        mensajes.add(new DTOMensaje(
                            mensaje.getId(),
                            mensaje.getContenido(),
                            mensaje.getFecha(),
                            TipoDestinatario.GRUPO.ordinal(),
                            mensaje.getId_dest_grupo(),
                            mensaje.getId_autor()
                        ));
                    });
                    
                    DTOListaMensajes listaMensajes = new DTOListaMensajes(mensajes);

                    // Enviar lista de mensajes.
                    String jsonResultado = gson.toJson(listaMensajes);

                    resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                    resultado.setCuerpoJSON(jsonResultado);
                }
                else 
                {
                    // El contacto no existe.
                    resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                    resultado.setCuerpoJSON(ERR_INV_NO_EXISTE);
                }
            }
            else 
            {
                final UsuarioDAO usuarioDAO = new UsuarioDAO();
                Usuario usuario = usuarioDAO.busqueda_porId(contactoDeMensajes.getIdDestinatario());
                
                if (usuario != null)
                {
                    // Obtener mensajes enviados al contacto.
                    List<Mensaje> mensajesUsuario = mensajeDAO.busqueda_DestUsuario(usuario);
                    List<DTOMensaje> mensajes = new ArrayList<>();
                    
                    mensajesUsuario.forEach((mensaje) -> {
                        mensajes.add(new DTOMensaje(
                            mensaje.getId(),
                            mensaje.getContenido(),
                            mensaje.getFecha(),
                            TipoDestinatario.GRUPO.ordinal(),
                            mensaje.getId_dest_grupo(),
                            mensaje.getId_autor()
                        ));
                    });
                    
                    DTOListaMensajes listaMensajes = new DTOListaMensajes(mensajes);

                    // Enviar lista de mensajes.
                    String jsonResultado = gson.toJson(listaMensajes);

                    resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                    resultado.setCuerpoJSON(jsonResultado);
                }
                else 
                {
                    // El contacto no existe.
                    resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                    resultado.setCuerpoJSON(ERR_INV_NO_EXISTE);
                }
            }
        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(ERR_FORMATO_JSON);
        }
        
        return resultado;
    }

    public EventoServidor enviarMensaje(String json) 
    {
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR,
            idUsuario.get(),
            ""
        );
        
        try 
        {
            DTONuevoMensaje nuevoMensaje = gson.fromJson(json, DTONuevoMensaje.class);
            
            Mensaje mensaje = new Mensaje(
                nuevoMensaje.getContenido(), 
                idUsuario.get(), 
                nuevoMensaje.getIdDestinatario(), 
                nuevoMensaje.getIdDestinatario()
            );

            //TODO: Revisar si existe el usuario autor y el destinatario en BD.
            Usuario autorMensaje = usuarioDAO.busqueda_porId(idUsuario.get());

            if (autorMensaje != null) 
            {                
                if (nuevoMensaje.getTipoDestinatario() == TipoDestinatario.GRUPO)
                {
                    Usuario destinatario = usuarioDAO.busqueda_porId(nuevoMensaje.getIdDestinatario());
                    
                    if (destinatario != null)
                    {
                        // Registrar nuevo mensaje en BD.
                        int idMensajeCreado = mensajeDAO.crear_paraUsuario(mensaje);

                        DTOMensaje mensajeEnviado = new DTOMensaje(
                            idMensajeCreado,
                            nuevoMensaje.getContenido(),
                            nuevoMensaje.getTipoDestinatario().ordinal(),
                            nuevoMensaje.getIdDestinatario(),
                            idUsuario.get()
                        );

                        // Notificar a destinatarios, enviarles el mensaje.
                        estadoServidor.enviarMensaje(mensajeEnviado);
                        
                        String jsonMensaje = gson.toJson(mensajeEnviado);

                        // El mensaje fue enviado.
                        resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                        resultado.setCuerpoJSON(jsonMensaje);
                    } else {
                        // El destinatario del mensaje no existe.
                        String jsonErr = gson.toJson(new ErrorEvento(ERR_DEST_NO_EXISTE));
                
                        resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                        resultado.setCuerpoJSON(jsonErr);
                    }
                }
                else 
                {
                    Usuario destinatario = usuarioDAO.busqueda_porId(nuevoMensaje.getIdDestinatario());
                    
                    if (destinatario != null)
                    {
                        // Registrar nuevo mensaje en BD.
                        int idMensajeCreado = mensajeDAO.crear_paraUsuario(mensaje);

                        DTOMensaje mensajeEnviado = new DTOMensaje(
                            idMensajeCreado,
                            nuevoMensaje.getContenido(),
                            nuevoMensaje.getTipoDestinatario().ordinal(),
                            nuevoMensaje.getIdDestinatario(),
                            idUsuario.get()
                        );

                        // Notificar a destinatarios, enviarles el mensaje.
                        estadoServidor.enviarMensaje(mensajeEnviado);
                        
                        String jsonMensaje = gson.toJson(mensajeEnviado);

                        // El mensaje fue enviado.
                        resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                        resultado.setCuerpoJSON(jsonMensaje);
                    } else {
                        // El destinatario del mensaje no existe.
                        String jsonErr = gson.toJson(new ErrorEvento(ERR_DEST_NO_EXISTE));
                
                        resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                        resultado.setCuerpoJSON(jsonErr);
                    }
                }
            } else {
                // El usuario no existe, retornar error.
                String jsonErr = gson.toJson(new ErrorEvento(ERR_USR_NO_EXISTE));
                
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(jsonErr);
            }

        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            String jsonErr = gson.toJson(new ErrorEvento(ERR_FORMATO_JSON));
            
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(jsonErr);
        }
        
        return resultado;
    }

   public EventoServidor enviarInvitacion(String json) 
    {
        try 
        {
            DTOInvitacion dtoInvitacion = gson.fromJson(json, DTOInvitacion.class);

            // Revisar si existen el usuario invitado y el grupo.
            Usuario usuarioQueInvita = usuarioDAO.busqueda_porId(dtoInvitacion.getIdUsuarioQueInvita());
                        
            // Un grupo es valido si la invitacion es de amistad, o si existe el grupo.
            boolean grupoValido = (dtoInvitacion.esDeAmistad()) ? true : true;

            if (usuarioQueInvita != null && grupoValido) 
            {
                // Crear invitacion en BD.
                Invitacion invitacion = new Invitacion(
                    dtoInvitacion.getIdUsuarioInvitado(),
                    dtoInvitacion.getIdGrupo(),
                    dtoInvitacion.getIdUsuarioQueInvita()
                );
                
                int idInvitacion = (dtoInvitacion.esDeAmistad()) 
                        ? invitacionDAO.crear_paraAmistad(invitacion)
                        : invitacionDAO.crear_paraGrupo(invitacion);
                
                dtoInvitacion.setId(idInvitacion);
                
                // Notificar sobre la nueva invitación.
                estadoServidor.enviarInvitacion(dtoInvitacion);

                // Invitacion creada.
                return new EventoServidor(
                    TipoDeEvento.RESULTADO_OK,
                    idUsuario.get(),
                    gson.toJson(dtoInvitacion)
                );

            } else {
                // El grupo o el usuario invitado no existen.
                return new EventoServidor(
                    TipoDeEvento.ERROR_CLIENTE,
                    idUsuario.get(),
                    ERR_USR_O_GRUP_NO_EXISTEN
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new EventoServidor(TipoDeEvento.ERROR_CLIENTE, idUsuario.get(), ERR_FORMATO_JSON);
        }
    }

    public EventoServidor aceptarInvitacion(String json) 
    {
        // La respuesta que será enviada al cliente.
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR, 
            idUsuario.get(), 
            ""
        );
        
        try 
        {
            DTOIdEntidad idInvitacion = gson.fromJson(json, DTOIdEntidad.class);

            // Intentar obtener la invitación con idInvitacion desde la BD.
            Invitacion invitacion = invitacionDAO.busqueda_porId(idInvitacion.getId());
            
            if (invitacion == null)
            {
                // La invitación no existe, no puede ser aceptada.
                String jsonErr = gson.toJson(new ErrorEvento(ERR_INV_NO_EXISTE));
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(jsonErr);
                
                return resultado;
            }
            
            // Intentar obtener el usuario que aceptó la invitación desde la BD.
            Usuario usuarioQueAcepto = usuarioDAO.busqueda_porId(invitacion.getId_usuario_emisor());
                        
            if (usuarioQueAcepto == null)
            {
                // El usuario que intenta aceptar la invitación debe existir.
                String jsonErr = gson.toJson(new ErrorEvento(ERR_USR_EXISTE));
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(jsonErr);
                
                return resultado;
            }

            boolean mismoUsuarioQueAcepta = usuarioQueAcepto.getId() == invitacion.getId_usuario_invitado();

            // Si la invitación y el usuario existen en BD, aceptar la invitación.
            if (mismoUsuarioQueAcepta)
            {
                DTOInvitacion dtoInvitacion = DTOInvitacion.desdeModelo(invitacion);
                
                if (dtoInvitacion.esDeAmistad())
                {
                    // Crear en BD amistad entre usuario actual y el usuario que 
                    // envio la invitacion.
                    Amistad amistad = new Amistad(
                            dtoInvitacion.getIdUsuarioQueInvita(), 
                            dtoInvitacion.getIdUsuarioInvitado()
                    );
                    
                    AmistadDAO amistadDAO = new AmistadDAO();
                    amistadDAO.crear(amistad);
                }
                else 
                {
                    // Agregar en BD al usuario como miembro del grupo.
                    Grupo grupo = grupoDAO.buscar_porId(dtoInvitacion.getIdGrupo());
                    
                    UsuariosGrupo usuariosGrupo = new UsuariosGrupo(
                        dtoInvitacion.getIdUsuarioInvitado(),
                        grupo.getId()
                    );
                    
                    usuariosGrupoDAO.crear(usuariosGrupo);
                }

                DTOInvAceptada invitacionAceptada = new DTOInvAceptada(
                    dtoInvitacion,
                    DTOUsuario.desdeModelo(usuarioQueAcepto)
                );

                String jsonResultado = gson.toJson(invitacionAceptada);

                // Notificar al otro usuario sobre su nueva amistad, o al grupo
                // sobre su nuevo miembro.
                estadoServidor.invitacionAceptada(invitacionAceptada);

                // Eliminar invitacion aceptada de la BD. 
                invitacionDAO.eliminar(invitacion);
                
                resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                resultado.setCuerpoJSON(jsonResultado);
            }
            else 
            {
                // El usuario invitado y el que está intentando aceptar la 
                // invitación no son el mismo.
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_AUTENTICACION);
                resultado.setCuerpoJSON(ERR_INV_NO_EXISTE);
            }
        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(ERR_FORMATO_JSON);
        }
        
        return resultado;
    }
    
    public EventoServidor rechazarInvitacion(String json) 
    {
        // La respuesta que será enviada al cliente.
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR, 
            idUsuario.get(), 
            ""
        );
        
        try 
        {
            DTOIdEntidad idInvitacion = gson.fromJson(json, DTOIdEntidad.class);

            // Intentar obtener la invitación con idInvitacion desde la BD.
            Invitacion invitacionRechazada = invitacionDAO.busqueda_porId(idInvitacion.getId());
            
            if (invitacionRechazada != null) 
            {                
                DTOInvitacion dtoInvitacion = DTOInvitacion.desdeModelo(invitacionRechazada);
                
                // Enviar notificacion de invitacion rechazada.
                estadoServidor.invitacionRechazada(dtoInvitacion);
                
                // Eliminar invitacion rechazada. 
                invitacionDAO.eliminar(invitacionRechazada);
                
                // Enviar confirmacion de id de la invitacion rechazada al cliente.
                String jsonResultado = gson.toJson(idInvitacion);
                
                resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                resultado.setCuerpoJSON(jsonResultado);

            } else {
                // La invitación no existe, no puede ser rechazada.
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(ERR_INV_NO_EXISTE);
            }
        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(ERR_FORMATO_JSON);
        }
        
        return resultado;
    }
    
    public EventoServidor crearGrupo(String json) 
    {
        // La respuesta que será enviada al cliente.
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR, 
            idUsuario.get(), 
            ""
        );
        
        try 
        {
            DTONuevoGrupo nuevoGrupo = gson.fromJson(json, DTONuevoGrupo.class);

            if (nuevoGrupo.getIdsUsuariosMiembro().size() >= 2) 
            {
                //TODO Revisar que todos los miembros iniciales existan.
                boolean miembrosExisten = true;
                
                for (Integer idNuevoMiembro : nuevoGrupo.getIdsUsuariosMiembro())
                {
                    Usuario miembro = usuarioDAO.busqueda_porId(idNuevoMiembro);
                    miembrosExisten = (miembro != null);
                }

                if (miembrosExisten)
                {
                    // Crear nuevo grupo.
                    Grupo grupo = new Grupo(nuevoGrupo.getNombre());
                    
                    int idNuevoGrupo = grupoDAO.crear(grupo);
                    
                    UsuariosGrupoDAO usuariosGrupo = new UsuariosGrupoDAO();
                    
                    // Crear relaciones y enviar invitacion a miembros iniciales.
                    for (int idMiembro : nuevoGrupo.getIdsUsuariosMiembro())
                    {
                        usuariosGrupo.crear(new UsuariosGrupo(
                            idMiembro,
                            idNuevoGrupo
                        ));
                        
                        DTOInvitacion invitacion = new DTOInvitacion(idUsuario.get(), idMiembro, idNuevoGrupo);
                        estadoServidor.enviarInvitacion(invitacion);
                    }
                    
                    // Enviar confirmacion con datos del grupo creado al cliente.
                    DTOGrupo grupoCreado = new DTOGrupo(idNuevoGrupo, nuevoGrupo.getNombre(), nuevoGrupo.getIdsUsuariosMiembro());
                    String jsonResultado = gson.toJson(grupoCreado);

                    resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                    resultado.setCuerpoJSON(jsonResultado);
                    
                } else {
                    // Al menos uno de los miembros invitados incialmente no existe.
                    resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                    resultado.setCuerpoJSON(ERR_USR_NO_EXISTE);
                }

            } else {
                // El grupo no tiene el tamaño necesario.
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(ERR_GRUPO_USUARIOS_INSUF);
            }
        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(ERR_FORMATO_JSON);
        }
        
        return resultado;
    }

    public EventoServidor abandonarGrupo(String json) 
    {
        // La respuesta que será enviada al cliente.
        EventoServidor resultado = new EventoServidor(
            TipoDeEvento.ERROR_SERVIDOR, 
            idUsuario.get(), 
            ""
        );
        
        try 
        {
            DTOIdEntidad idGrupoAbandonado = gson.fromJson(json, DTOIdEntidad.class);

            // Revisar si existe el grupo y si el usuario es miembro.
            Grupo grupo = grupoDAO.buscar_porId(idGrupoAbandonado.getId());

            if (grupo != null) 
            {
                List<UsuariosGrupo> usuariosEnGrupo = usuariosGrupoDAO.busqueda_porGrupo(grupo.getId());
                
                UsuariosGrupo usuarioMiembro = null;
                
                for (UsuariosGrupo usuarioEnGrupo : usuariosEnGrupo)
                {
                    if (usuarioEnGrupo.getId_usuario_miembro() == idUsuario.get())
                    {
                        usuarioMiembro = usuarioEnGrupo;
                        break;
                    }
                }
                
                if (usuarioMiembro != null)
                {
                    // Eliminar usuario del grupo.
                    usuariosGrupoDAO.eliminar(usuarioMiembro);
                    
                    DTOGrupo dtoGrupo = new DTOGrupo(grupo.getId(), grupo.getNombre(), new ArrayList<>());

                    // Enviar notificación de usuario abandono grupo.
                    estadoServidor.usuarioAbandono(new DTOAbandonarGrupo(idUsuario.get(), dtoGrupo));

                    // Eliminar grupo, si tiene menos de 3 miembros.
                    if (usuariosEnGrupo.size() < 3)
                    {
                        grupoDAO.eliminar(grupo);
                        
                        // Enviar notificación de grupo eliminado.
                        estadoServidor.grupoEliminado(grupo.getId());
                    }

                    // Enviar confirmacion con datos del grupo abandonado al cliente.
                    String jsonResultado = gson.toJson(idGrupoAbandonado);

                    resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                    resultado.setCuerpoJSON(jsonResultado);
                } else {
                    // El usuario no es miembro del grupo, no lo puede abandonar.
                    resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                    resultado.setCuerpoJSON(ERR_GRUPO_USR_NO_ES_MIEMBRO);
                }
            } else {
                // El grupo no existe.
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(ERR_INV_NO_EXISTE);
            }
        } 
        catch (JsonSyntaxException e)
        {
            // El cuerpo de la acción del cliente no está bien formado.
            resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
            resultado.setCuerpoJSON(ERR_FORMATO_JSON);
        }
        
        return resultado;
    }
}
