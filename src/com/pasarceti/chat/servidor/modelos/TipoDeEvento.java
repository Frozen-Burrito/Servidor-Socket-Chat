package com.pasarceti.chat.servidor.modelos;

/**
 * Es un tipo de evento del servidor, que sirve para notificar a los clientes
 * de un cambio realizado en el sistema.
 * 
 */
public enum TipoDeEvento {
    RESULTADO_OK, 
    ERROR_CLIENTE,
    ERROR_SERVIDOR,
    ERROR_AUTENTICACION,
    USUARIO_CONECTADO,
    USUARIO_DESCONECTADO,
    MENSAJE_ENVIADO,
    INVITACION_ENVIADA,
    AMISTAD_ACEPTADA,
    AMISTAD_RECHAZADA,
    USUARIO_SE_UNIO_A_GRUPO,
    USUARIO_ABANDONO_GRUPO,
    GRUPO_ELIMINADO,
}
