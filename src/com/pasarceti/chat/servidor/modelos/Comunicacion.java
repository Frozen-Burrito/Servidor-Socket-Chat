package com.pasarceti.chat.servidor.modelos;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * @brief Representa una comunicación (petición o respuesta) procesada por el 
 * servidor. Ayuda a asegurar que toda la comunicación producida por el servidor 
 * en los sockets tenga un formato correcto.
 * 
 */
public class Comunicacion 
{ 
    public static final String IDENTIFICADOR_COM = "CHAT"; 

    public static final int LONGITUD_MAX_CUERPO = 1024;

    protected boolean fueExitosa;

    protected int longitudCuerpo; 

    protected int idUsuarioCliente;

    protected String cuerpoJSON;

    public Comunicacion(boolean fueExitosa, int longitud, int idUsuarioCliente, String cuerpoJSON) 
    {
        this.fueExitosa = fueExitosa;
        this.longitudCuerpo = longitud;
        this.idUsuarioCliente = idUsuarioCliente;
        this.cuerpoJSON = cuerpoJSON;
    }

    public boolean tieneJson() 
    {
        return this.longitudCuerpo > 0;
    }

    public static void validarPrimeraLinea(String[] primeraLinea) throws IllegalArgumentException
    {
        if (primeraLinea.length < 1 || !primeraLinea[0].equals(IDENTIFICADOR_COM))
        {
            throw new IllegalArgumentException("La petición no es reconocida por el servidor.");
        }

        if (primeraLinea.length < 4) 
        {
            throw new IllegalArgumentException("La peticion no tiene todos los datos.");
        }

        if (primeraLinea.length > 5) 
        {
            throw new IllegalArgumentException("La peticion tiene demasiados valores.");
        }
    }

    public static void validarLongitudCuerpo(int longitud, String cuerpoJSON) throws IllegalArgumentException
    {
        if (longitud != cuerpoJSON.length()) 
        {
            throw new IllegalArgumentException("El cuerpo y la longitud no coinciden.");
        }
    }

    public boolean fueExitosa() {
        return fueExitosa;
    }

    public void setFueExitosa(boolean fueExitosa) {
        this.fueExitosa = fueExitosa;
    }

    public int getLongitudCuerpo() {
        return longitudCuerpo;
    }

    public void setLongitudCuerpo(int longitudCuerpo) {
        this.longitudCuerpo = longitudCuerpo;
    }

    public int getIdUsuarioCliente() {
        return idUsuarioCliente;
    }

    public void setIdUsuarioCliente(int idUsuarioCliente) {
        this.idUsuarioCliente = idUsuarioCliente;
    }

    public String getCuerpoJSON() {
        return cuerpoJSON;
    }

    public void setCuerpoJSON(String cuerpoJSON) throws IllegalArgumentException
    {
        validarLongitudCuerpo(longitudCuerpo, cuerpoJSON);

        this.cuerpoJSON = cuerpoJSON;
    }
}
