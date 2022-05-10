package com.pasarceti.chat.servidor.modelos;

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

    private int tipoDeEvento; 

    private boolean fueExitosa;

    private int longitudCuerpo; 

    private int idUsuarioCliente;

    private String cuerpoJSON;

    public Comunicacion(int tipoDeEvento, boolean fueExitosa, int idUsuarioCliente, String cuerpoJSON) {
        this.tipoDeEvento = tipoDeEvento;
        this.fueExitosa = fueExitosa;
        this.longitudCuerpo = cuerpoJSON.length();
        this.idUsuarioCliente = idUsuarioCliente;
        this.cuerpoJSON = cuerpoJSON;
    }

    @Override
    public String toString() 
    {
        String lineaInicial = String.format(
            "%s %i %s %i %i\n", 
            IDENTIFICADOR_COM, 
            tipoDeEvento, 
            String.valueOf(fueExitosa), 
            longitudCuerpo, 
            idUsuarioCliente
        );

        return String.format("%s\n%s", lineaInicial, cuerpoJSON);
    }

    public int getTipoDeEvento() {
        return tipoDeEvento;
    }

    public void setTipoDeEvento(int tipoDeEvento) {
        this.tipoDeEvento = tipoDeEvento;
    }

    public boolean isFueExitosa() {
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

    public void setCuerpoJSON(String cuerpoJSON) {
        this.cuerpoJSON = cuerpoJSON;
    }
}
