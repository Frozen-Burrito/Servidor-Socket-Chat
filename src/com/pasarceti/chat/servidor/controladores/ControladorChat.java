package com.pasarceti.chat.servidor.controladores;

import java.net.Socket;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pasarceti.chat.servidor.modelos.AccionCliente;
import com.pasarceti.chat.servidor.modelos.Cliente;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.TipoDestinatario;
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

    public ControladorChat(EstadoServidor estadoServidor, ThreadLocal<Integer> idUsuario)
    {
        this.estadoServidor = estadoServidor;
        this.idUsuario = idUsuario;
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
            case OBTENER_MENSAJES return obtenerMensajes(datosJson);
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
        try 
        {
            DTOCredUsuario datosRegistro = gson.fromJson(json, DTOCredUsuario.class);

            //TODO: Revisar si un usuario con el mismo nombre aún no existe en la BD.
            boolean usuarioNoExiste = true;

            if (usuarioNoExiste) 
            {
                // El usuario no ha sido creado, registrar un nuevo usuario.
                //TODO: Crear usuario en BD.
                int idNuevoUsuario = 0;

                // Usuario creado, registro exitoso.
                return new EventoServidor(
                    TipoDeEvento.RESULTADO_OK,
                    idNuevoUsuario,
                    "El usuario fue registrado con éxito."
                );

            } else {
                // Ya existe un usuario con este nombre de usuario, retornar error.
                return new EventoServidor(
                    TipoDeEvento.ERROR_CLIENTE, 
                     -1,
                    ERR_USR_EXISTE + " " + datosRegistro.getNombreUsuario()
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new EventoServidor(TipoDeEvento.ERROR_CLIENTE, -1, ERR_FORMATO_JSON);
        }
        catch (Exception e) 
        {
            return new EventoServidor(TipoDeEvento.ERROR_SERVIDOR, -1, ERR_GENERAL);
        }
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

            //TODO: Revisar si ya existe el usuario en la BD.
            boolean usuarioExiste = true;

            if (usuarioExiste) 
            {
                // El usuario ya ha sido creado, comparar las contraseñas.
                //TODO: Comparar con la contraseña real, cifrada.
                String passDelUsuario = "holamundo";

                boolean passCoinciden = passDelUsuario.equals(credenciales.getPassword());

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
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(ERR_USR_NO_EXISTE + " " + credenciales.getNombreUsuario());
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

    public EventoServidor cerrarSesion() 
    {
        //TODO: Obtener datos del usuario, asegurar que existe en lista de usuarios conectados. 
        String nombreUsrExistente = "usuarioTemporal";

        // Actualizar estado con la desconexión del usuario actual.
        estadoServidor.desconectarUsuario(idUsuario);
        
        // Usuario cerró sesión.
        return new EventoServidor(
            TipoDeEvento.RESULTADO_OK,
            -1,
            "El Usuario \"" + nombreUsrExistente + "\" cerró sesión"
        );
    }

    public EventoServidor recuperarPassword(String json) 
    {
        try 
        {
            DTOCambioPassword solicitudCambio = gson.fromJson(json, DTOCambioPassword.class);

            //TODO: Asegurar que el usuario ya existe en la BD
            boolean usuarioYaExiste = true;

            if (usuarioYaExiste) 
            {
                //TODO: Actualizar registro de usuario con nueva contraseña en BD.

                // Usuario creado, registro exitoso.
                return new EventoServidor(
                    TipoDeEvento.RESULTADO_OK,
                    idUsuario.get(),
                    "El usuario cambió su cuenta con éxito."
                );

            } else {
                // El usuario ya existe, retornar error.
                return new EventoServidor(
                    TipoDeEvento.ERROR_CLIENTE, 
                    idUsuario.get(),
                    ERR_USR_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new EventoServidor(TipoDeEvento.ERROR_CLIENTE, idUsuario.get(), ERR_FORMATO_JSON);
        }
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
            
            boolean contactoExiste = true;
            
            if (contactoDeMensajes.getTipoDestinatario() == TipoDestinatario.GRUPO)
            {
                final DTOGrupo grupoContacto = new DTOGrupo(0, "Grupo de prueba", new ArrayList<>());
                
                contactoExiste = grupoContacto != null;
            }
            else 
            {
                final DTOUsuario usuarioContacto = new DTOUsuario(0, "Usuario");
                
                contactoExiste = usuarioContacto != null;
            }

            if (contactoExiste) 
            {
                //TODO: Obtener mensajes del contacto.
                DTOListaMensajes mensajes = new DTOListaMensajes(new ArrayList<>());

                // Enviar lista de mensajes.
                String jsonResultado = gson.toJson(mensajes);

                resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                resultado.setCuerpoJSON(jsonResultado);

            } else {
                // El contacto no existe.
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

    public EventoServidor enviarMensaje(String json) 
    {
        try 
        {
            DTONuevoMensaje mensaje = gson.fromJson(json, DTONuevoMensaje.class);

            //TODO: Revisar si existe el usuario autor y el destinatario en BD.
            boolean usuarioExiste = true;
            boolean destinatarioExiste = true;

            if (usuarioExiste) 
            {
                if (destinatarioExiste)
                {
                    //TODO: Registrar nuevo mensaje en BD.
                    int idMensajeCreado = 0;
                    DTOMensaje mensajeEnviado = new DTOMensaje(
                        idMensajeCreado,
                        mensaje.getContenido(),
                        mensaje.getTipoDestinatario().ordinal(),
                        mensaje.getIdDestinatario(),
                        idUsuario.get()
                    );
                    
                    // Notificar a destinatarios, enviarles el mensaje.
                    estadoServidor.enviarMensaje(mensajeEnviado);

                    // El mensaje fue enviado.
                    return new EventoServidor(
                        TipoDeEvento.RESULTADO_OK,
                        idUsuario.get(),
                        gson.toJson(mensaje)
                    );
                } else {
                    // El destinatario del mensaje no existe.
                    return new EventoServidor(
                        TipoDeEvento.ERROR_CLIENTE, 
                        idUsuario.get(),
                        ERR_DEST_NO_EXISTE
                    );
                }

            } else {
                // El usuario no existe, retornar error.
                return new EventoServidor(
                    TipoDeEvento.ERROR_CLIENTE, 
                    idUsuario.get(),
                    ERR_USR_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new EventoServidor(TipoDeEvento.ERROR_CLIENTE, idUsuario.get(), ERR_FORMATO_JSON);
        }
    }

   public EventoServidor enviarInvitacion(String json) 
    {
        try 
        {
            DTOInvitacion invitacion = gson.fromJson(json, DTOInvitacion.class);

            //TODO: Revisar si existen el usuario invitado y el grupo.
            boolean usuarioInvExiste = true;
            
            // Un grupo es valido si la invitacion es de amistad, o si existe el grupo.
            boolean grupoValido = (invitacion.esDeAmistad()) ? true : true;

            if (usuarioInvExiste && grupoValido) 
            {
                //TODO: Crear invitacion en BD.
                int idInvitacion = 1;
                
                invitacion.setId(idInvitacion);
                
                // Notificar sobre la nueva invitación.
                estadoServidor.enviarInvitacion(invitacion);

                // Invitacion creada.
                return new EventoServidor(
                    TipoDeEvento.RESULTADO_OK,
                    idUsuario.get(),
                    gson.toJson(invitacion)
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

            //TODO: Intentar obtener la invitación con idInvitacion desde la BD.
            DTOInvitacion invitacion = new DTOInvitacion(idInvitacion.getId(), idUsuario.get(), null);

            //TODO: Intentar obtener el usuario que invitó desde la BD.
            DTOUsuario usuarioQueAcepto = new DTOUsuario(2, "Juan");
            
            boolean existen = invitacion != null && usuarioQueAcepto != null;

            if (existen) 
            {
                boolean mismoUsuarioQueAcepta = usuarioQueAcepto.getIdUsuario() == invitacion.getIdUsuarioInvitado();

                if (mismoUsuarioQueAcepta)
                {
                    // Si la invitación y el usuario existen en BD, aceptarla.
                    if (invitacion.esDeAmistad())
                    {
                        //TODO: Crear en BD amistad entre usuario actual y el usuario que 
                        // envio la invitacion.
                    }
                    else 
                    {
                        //TODO: Agregar en BD al usuario como miembro del grupo.
                    }

                    DTOInvAceptada invitacionAceptada = new DTOInvAceptada(
                        invitacion,
                        usuarioQueAcepto
                    );

                    String jsonResultado = gson.toJson(invitacionAceptada);

                    // Notificar al otro usuario sobre su nueva amistad, o al grupo
                    // sobre su nuevo miembro.
                    estadoServidor.invitacionAceptada(invitacionAceptada);

                    //TODO: Eliminar invitacion aceptada de la BD. 

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

            } else {
                // La invitación no existe, no puede ser aceptada.
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

            //TODO: Intentar obtener la invitación con idInvitacion desde la BD.
            DTOInvitacion invitacionRechazada = new DTOInvitacion(idInvitacion.getId(), idUsuario.get(), null);

            if (invitacionRechazada != null) 
            {                
                //TODO: Enviar notificacion de invitacion rechazada.
                estadoServidor.invitacionRechazada(invitacionRechazada);
                
                //TODO: Eliminar invitacion rechazada. 
                
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
                //TODO: Revisar que todos los miembros iniciales existan.
                boolean miembrosExisten = true;

                if (miembrosExisten)
                {
                    //TODO: Crear nuevo grupo.
                    int idNuevoGrupo = 0;
                    
                    // Enviar invitacion a miembros iniciales.
                    for (int idMiembro : nuevoGrupo.getIdsUsuariosMiembro())
                    {
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

            //TODO: Revisar si existe el grupo y si el usuario es miembro.
            DTOGrupo grupo = new DTOGrupo(idGrupoAbandonado.getId(), "Grupo de prueba", new ArrayList<>());
            boolean usuarioEsMiembro = true;

            if (grupo != null && usuarioEsMiembro) 
            {
                //TODO: Eliminar usuario del grupo.
                
                // Enviar notificación de usuario abandono grupo.
                estadoServidor.usuarioAbandono(new DTOAbandonarGrupo(idUsuario.get(), grupo));
                
                //TODO: Eliminar grupo, si tiene menos de 3 miembros.
                if (grupo.getIdsUsuariosMiembro().size() < 3)
                {
                    // Enviar notificación de grupo eliminado.
                    estadoServidor.grupoEliminado(grupo.getId());
                }

                // Enviar confirmacion con datos del grupo abandonado al cliente.
                String jsonResultado = gson.toJson(idGrupoAbandonado);

                resultado.setTipoDeEvento(TipoDeEvento.RESULTADO_OK);
                resultado.setCuerpoJSON(jsonResultado);

            } else if (!usuarioEsMiembro) {
                // El usuario no es miembro del grupo, no lo puede abandonar.
                resultado.setTipoDeEvento(TipoDeEvento.ERROR_CLIENTE);
                resultado.setCuerpoJSON(ERR_GRUPO_USR_NO_ES_MIEMBRO);
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
