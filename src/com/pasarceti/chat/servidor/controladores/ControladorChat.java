package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pasarceti.chat.servidor.modelos.AccionCliente;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.EventoServidor;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.dto.DTOCambioPassword;
import com.pasarceti.chat.servidor.modelos.dto.DTOCredUsuario;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOIdGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOModInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Permite a los clientes realizar acciones con el chat, que producen un resultado.
 * 
 * Algunas acciones, como enviar un mensaje, producen también un cambio en el
 * estado del servidor, que es propagado a los demás clientes que deban ser
 * "notificados" de ese cambio.
 * 
 */
public class ControladorChat 
{
    private static final String ERR_GENERAL = "Hubo un error procesando la petición del cliente.";
    private static final String ERR_FORMATO_JSON = "El JSON en el cuerpo de la petición no tiene el formato correcto.";
    private static final String ERR_USR_EXISTE = "Ya existe un usuario con este nombre de usuario:";
    private static final String ERR_USR_NO_EXISTE = "No existe un usuario con este nombre de usuario:";
    private static final String ERR_USR_PASS_INCORRECTO = "La contraseña es incorrecta.";

    private static final String ERR_USR_O_GRUP_NO_EXISTEN = "El usuario o grupo no existen.";
    private static final String ERR_GRUPO_NO_EXISTE = "El grupo no existe";
    private static final String ERR_DEST_NO_EXISTE = "El destinatario del mensaje no fue encontrado";
    private static final String ERR_INV_NO_EXISTE = "La invitación para el usuario no existe.";
    private static final String ERR_GRUPO_USUARIOS_INSUF = "El grupo tiene menos usuarios que el mínimo (3) para mantenerlo";

    private final EstadoServidor estadoServidor;

    public ControladorChat(EstadoServidor estadoServidor)
    {
        this.estadoServidor = estadoServidor;
    }

    /**
     * Procesa una acción recibida de un cliente, determina si es correcta,
     * accede y/o modifica la BD y produce un resultado según la acción.
     *  
     * @param accionCliente La acción solicitada por el cliente.
     * @return Un evento del servidor con la respuesta a la acción
     */
    public EventoServidor ejecutarAccion(AccionCliente accionCliente)
    {
        final int idUsuarioRecibido = accionCliente.getIdUsuarioCliente();
        final String datosJson = accionCliente.getCuerpoJSON();

        switch (accionCliente.getTipoDeAccion())
        {
        case REGISTRAR_USUARIO: return registrarUsuario(datosJson);
        case INICIAR_SESION: return iniciarSesion(datosJson);
        case CERRAR_SESION: return cerrarSesion(idUsuarioRecibido);
        case RECUPERAR_CONTRASEÑA: return recuperarPassword(idUsuarioRecibido, datosJson);
        case ENVIAR_MENSAJE: return enviarMensaje(idUsuarioRecibido, datosJson);
        case AGREGAR_AMIGO: return agregarAmistad(idUsuarioRecibido, datosJson);
        case ELIMINAR_AMIGO: return removerAmistad(idUsuarioRecibido, datosJson);
        case CREAR_GRUPO: return crearGrupo(idUsuarioRecibido, datosJson);
        case ENVIAR_INVITACION: return enviarInvitacion(idUsuarioRecibido, datosJson);
        case ACEPTAR_INVITACION: return rechazarInvitacion(idUsuarioRecibido, datosJson);
        case RECHAZAR_INVITACION: return aceptarInvitacion(idUsuarioRecibido, datosJson);
        case ABANDONAR_GRUPO: return abandonarGrupo(idUsuarioRecibido, datosJson);
        default: return new EventoServidor(TipoDeEvento.ERROR_SERVIDOR, idUsuarioRecibido, ERR_GENERAL);
        }
    }

    private EventoServidor registrarUsuario(String json)
    {
        try 
        {
            Gson gson = new Gson();
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
        try 
        {
            Gson gson = new Gson();
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
                    int idUsuario = 0;
                    //TODO: Obtener datos del usuario (Amigos, grupos, invitaciones).

                    estadoServidor.agregarCliente(idUsuario, cliente);

                    // Usuario accedio con exito.
                    return new EventoServidor(
                        TipoDeEvento.USUARIO_CONECTADO,
                        idUsuario,
                        "El Usuario " + credenciales.getNombreUsuario() + " ha iniciado sesión"
                    );
                } else {
                    // La contraseña es incorrecta, retornar error.
                    return new Evento(
                        TipoDeEvento.ERROR_CLIENTE, 
                        ERR_USR_PASS_INCORRECTO
                    );
                }

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_NO_EXISTE + " " + credenciales.getNombreUsuario()
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor cerrarSesion(int idUsuario) 
    {
        //TODO: Obtener datos del usuario, asegurar que existe en lista de usuarios conectados. 
        String nombreUsrExistente = "usuarioTemporal";

        // Usuario cerró sesión.
        return new Evento(
            TipoDeEvento.USUARIO_DESCONECTADO,
            "El Usuario \"" + nombreUsrExistente + "\" ha iniciado sesión"
        );
    }

    public EventoServidor recuperarPassword(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOCambioPassword solicitudCambio = gson.fromJson(json, DTOCambioPassword.class);

            //TODO: Asegurar que el usuario ya existe en la BD
            boolean usuarioYaExiste = true;

            if (usuarioYaExiste) 
            {
                //TODO: Actualizar registro de usuario con nueva contraseña en BD.

                // Usuario creado, registro exitoso.
                return new Evento(
                    TipoDeEvento.PASSWORD_CAMBIADO,
                    ""
                );

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor enviarMensaje(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOMensaje mensaje = gson.fromJson(json, DTOMensaje.class);

            //TODO: Revisar si existe el usuario autor y el destinatario en BD.
            boolean usuarioExiste = true;
            boolean destinatarioExiste = true;

            if (usuarioExiste) 
            {
                if (destinatarioExiste)
                {
                    //TODO: Registrar nuevo mensaje en BD.
                    //TODO: Emviar mensaje a destinatario.

                    // El mensaje fue enviado.
                    return new Evento(
                        TipoDeEvento.MENSAJE_ENVIADO,
                        gson.toJson(mensaje)
                    );
                } else {
                    // El destinatario del mensaje no existe.
                    return new Evento(
                        TipoDeEvento.ERROR_CLIENTE, 
                        ERR_DEST_NO_EXISTE
                    );
                }

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor agregarAmistad(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOUsuario usuarioAmistad = gson.fromJson(json, DTOUsuario.class);

            //TODO: Revisar si el otro usuario existe en la BD.
            boolean usuarioExiste = true;

            if (usuarioExiste) 
            {
                //TODO: Obtener lista de amigos del usuario.
                List<DTOUsuario> amigosUsuario = new ArrayList<>();

                //TODO: Insertar nuevo registro de amistad

                //TODO: Actualizar lista de amigos.

                // Usuario creado, registro exitoso.
                return new Evento(
                    TipoDeEvento.AMIGO_AGREGADO,
                    gson.toJson(amigosUsuario)
                );

            } else {
                // El usuario que se intenta agregar como amigo no existe.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

   public EventoServidor removerAmistad(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOUsuario usuarioAmistad = gson.fromJson(json, DTOUsuario.class);

            //TODO: Revisar si el otro usuario existe en la lista de amigos BD.
            boolean usuarioExiste = true;

            if (usuarioExiste) 
            {
                //TODO: Obtener lista de amigos del usuario.
                List<DTOUsuario> amigosUsuario = new ArrayList<>();

                //TODO: Eliminar registro de amistad

                //TODO: Actualizar lista de amigos.

                // Usuario creado, registro exitoso.
                return new Evento(
                    TipoDeEvento.AMIGO_REMOVIDO,
                    gson.toJson(amigosUsuario)
                );

            } else {
                // El usuario que se intenta agregar como amigo no existe.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor crearGrupo(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOGrupo nuevoGrupo = gson.fromJson(json, DTOGrupo.class);

            if (nuevoGrupo.getIdsUsuariosMiembro().size() >= 2) 
            {
                //TODO: Revisar que todos los miembros existan.
                boolean miembrosExisten = true;

                if (miembrosExisten)
                {
                    //TODO: Crear nuevo grupo.
                    //TODO: Enviar invitaciones al grupo a cada miembro.

                    // Usuario accedio con exito.
                    return new Evento(
                        TipoDeEvento.GRUPO_CREADO,
                        gson.toJson(nuevoGrupo)
                    );
                } else {
                    // Al menos uno de los miembros no existe.
                    return new Evento(
                        TipoDeEvento.ERROR_CLIENTE, 
                        ERR_USR_NO_EXISTE
                    );
                }

            } else {
                // El grupo no tiene el tamaño necesario.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_GRUPO_USUARIOS_INSUF
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

   public EventoServidor enviarInvitacion(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOInvitacion invitacion = gson.fromJson(json, DTOInvitacion.class);

            //TODO: Revisar si existen el usuario invitado y el grupo.
            boolean usuarioInvExiste = true;
            boolean grupoExiste = true;

            if (usuarioInvExiste && grupoExiste) 
            {
                //TODO: Crear invitacion en BD.

                // Invitacion creada.
                return new Evento(
                    TipoDeEvento.INVITACION_ENVIADA,
                    gson.toJson(invitacion)
                );

            } else {
                // El grupo o el usuario invitado no existen.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_O_GRUP_NO_EXISTEN
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor rechazarInvitacion(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOModInvitacion invitacionRechazada = gson.fromJson(json, DTOModInvitacion.class);

            //TODO: Obtener invitaciones pendientes del usuario de la BD.
            List<DTOInvitacion> invitaciones = new ArrayList<>();

            //TODO: Revisar si existe la invitacion pendiente.
            boolean invitacionExiste = true;

            if (invitacionExiste) 
            {
                //TODO: Eliminar invitacion rechazada.

                // Invitación rechazada con éxito.
                return new Evento(
                    TipoDeEvento.INVITACION_RECHAZADA,
                    gson.toJson(invitaciones)
                );

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_INV_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor aceptarInvitacion(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOModInvitacion invitacionPendiente = gson.fromJson(json, DTOModInvitacion.class);

            //TODO: Obtener invitaciones pendientes del usuario de la BD.
            List<DTOInvitacion> invitaciones = new ArrayList<>();

            //TODO: Revisar si existe la invitacion pendiente.
            boolean invitacionExiste = true;

            if (invitacionExiste) 
            {
                //TODO: Agregar usuario como miembro del grupo.
                //TODO: Eliminar invitacion rechazada.

                // Invitación rechazada con éxito.
                return new Evento(
                    TipoDeEvento.INVITACION_ACEPTADA,
                    gson.toJson(invitaciones)
                );

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_INV_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public EventoServidor abandonarGrupo(int idUsuario, String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOIdGrupo grupoAbandonado = gson.fromJson(json, DTOIdGrupo.class);


            //TODO: Revisar si existe el grupo y si el usuario es miembro.
            boolean grupoExiste = true;
            boolean usuarioEsMiembro = true;

            if (grupoExiste && usuarioEsMiembro) 
            {
                //TODO: Eliminar usuario del grupo.

                //TODO: Eliminar grupo, si tiene menos de 3 miembros.
//                if (grupo.numMiembros < 3)

                // El usuario se salió del grupo.
                return new Evento(
                    TipoDeEvento.USUARIO_ABANDONO_GRUPO,
                    "El usuario abandonó el grupo."
                );

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_INV_NO_EXISTE
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }
}
