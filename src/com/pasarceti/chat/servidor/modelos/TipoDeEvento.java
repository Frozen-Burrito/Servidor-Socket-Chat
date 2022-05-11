package com.pasarceti.chat.servidor.modelos;

/**
 * Representa un tipo de evento del servidor de chat, para facilitar tratar las
 * funciones que debe cumplir.
 * 
 */
public enum TipoDeEvento {
    USUARIO_REGISTRADO, 
    USUARIO_CONECTADO,
    USUARIO_DESCONECTADO,
    PASSWORD_CAMBIADO,
    MENSAJE_ENVIADO,
    MENSAJE_RECIBIDO,
    AMIGO_AGREGADO,
    AMIGO_REMOVIDO,
    GRUPO_CREADO,
    GRUPO_ELIMINADO,
    INVITACION_ENVIADA,
    INVITACION_ACEPTADA,
    INVITACION_RECHAZADA,
    USUARIO_ABANDONO_GRUPO,
    ERROR_CLIENTE,
    ERROR_SERVIDOR,
}
