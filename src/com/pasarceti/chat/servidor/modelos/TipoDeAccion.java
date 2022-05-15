package com.pasarceti.chat.servidor.modelos;

/**
 * Identifica cada una de las acciones que puede realizar un cliente a través
 * del servidor.
 */
public enum TipoDeAccion {
    REGISTRAR_USUARIO,
    INICIAR_SESION,
    CERRAR_SESION,
    RECUPERAR_CONTRASEÑA,
    ENVIAR_MENSAJE,
    AGREGAR_AMIGO,
    ELIMINAR_AMIGO,
    CREAR_GRUPO,
    ENVIAR_INVITACION,
    ACEPTAR_INVITACION,
    RECHAZAR_INVITACION,
    ABANDONAR_GRUPO
}
