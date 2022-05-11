package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.dto.DTOCambioPassword;
import com.pasarceti.chat.servidor.modelos.dto.DTOCredUsuario;
import com.pasarceti.chat.servidor.modelos.dto.DTOGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOIdGrupo;
import com.pasarceti.chat.servidor.modelos.dto.DTOInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOMensaje;
import com.pasarceti.chat.servidor.modelos.dto.DTOModInvitacion;
import com.pasarceti.chat.servidor.modelos.dto.DTOUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 
 */
public class AccionesServidor 
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

    private static final AtomicInteger idDePrueba = new AtomicInteger();

    public static Evento registrarUsuario(String json)
    {
        try 
        {
            Gson gson = new Gson();
            DTOCredUsuario datosRegistro = gson.fromJson(json, DTOCredUsuario.class);

            //TODO: Revisar si ya existe el usuario en la BD.
            boolean usuarioNoExiste = true;

            if (usuarioNoExiste) 
            {
                //TODO: Crear usuario en BD.

                // Usuario creado, registro exitoso.
                return new Evento(
                    TipoDeEvento.USUARIO_REGISTRADO,
                    "Usuario registrado con exito"
                );

            } else {
                // El usuario ya existe, retornar error.
                return new Evento(
                    TipoDeEvento.ERROR_CLIENTE, 
                    ERR_USR_EXISTE + " " + datosRegistro.getNombreUsuario()
                );
            }

        } 
        catch (JsonSyntaxException e)
        {
            return new Evento(TipoDeEvento.ERROR_CLIENTE, ERR_FORMATO_JSON);
        }
    }

    public static Evento accederUsuario(String json) 
    {
        try 
        {
            Gson gson = new Gson();
            DTOCredUsuario credenciales = gson.fromJson(json, DTOCredUsuario.class);

            //TODO: Revisar si ya existe el usuario en la BD.
            boolean usuarioExiste = true;

            if (usuarioExiste) 
            {
                //TODO: Comparar con la contraseña real, cifrada.
                String passDelUsuario = "holamundo";

                boolean passCoinciden = passDelUsuario.equals(credenciales.getPassword());

                if (passCoinciden)
                {
                    //TODO: Obtener datos del usuario (Amigos, grupos, invitaciones).

                    // Usuario accedio con exito.
                    return new Evento(
                        TipoDeEvento.USUARIO_CONECTADO,
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

    public static Evento desconectarUsuario(int idUsuario) 
    {
        //TODO: Obtener datos del usuario, asegurar que existe en lista de usuarios conectados. 
        String nombreUsrExistente = "usuarioTemporal";

        // Usuario cerró sesión.
        return new Evento(
            TipoDeEvento.USUARIO_DESCONECTADO,
            "El Usuario \"" + nombreUsrExistente + "\" ha iniciado sesión"
        );
    }

    public static Evento cambiarPassword(int idUsuario, String json) 
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

    public static Evento enviarMensaje(int idUsuario, String json) 
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

    public static Evento recibirMensaje(int idUsuario) 
    {
        //TODO: Definir como se van a enviar los mensajes a los usuarios.
        return new Evento(
            TipoDeEvento.MENSAJE_RECIBIDO,
            "El grupo fue eliminado."
        );
    }

    public static Evento agregarAmistad(int idUsuario, String json) 
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

   public static Evento removerAmistad(int idUsuario, String json) 
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

    public static Evento crearGrupo(int idUsuario, String json) 
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

    public static Evento eliminarGrupo(int idUsuario) 
    {
        //TODO: Llamar esto como respuesta a cambios en cantidad de miembros. 
        return new Evento(
            TipoDeEvento.GRUPO_ELIMINADO,
            "El grupo fue eliminado."
        );
    }

   public static Evento enviarInvitacion(int idUsuario, String json) 
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

    public static Evento rechazarInvitacion(int idUsuario, String json) 
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

    public static Evento aceptarInvitacion(int idUsuario, String json) 
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

    public static Evento abandonarGrupo(int idUsuario, String json) 
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
