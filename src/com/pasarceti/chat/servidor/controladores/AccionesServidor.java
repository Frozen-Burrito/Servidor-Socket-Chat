package com.pasarceti.chat.servidor.controladores;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pasarceti.chat.servidor.modelos.Evento;
import com.pasarceti.chat.servidor.modelos.TipoDeEvento;
import com.pasarceti.chat.servidor.modelos.dto.DTOCredUsuario;
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
}
