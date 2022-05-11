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

    public Comunicacion(int tipoDeEvento, boolean fueExitosa, int longitud, int idUsuarioCliente, String cuerpoJSON) 
    {
        this.tipoDeEvento = tipoDeEvento;
        this.fueExitosa = fueExitosa;
        this.longitudCuerpo = longitud;
        this.idUsuarioCliente = idUsuarioCliente;
        this.cuerpoJSON = cuerpoJSON;
    }

    public static Comunicacion desdePeticion(String primeraLinea)
    {
        String[] partesLineaInicial = primeraLinea.split(" ");

        String mensajeErr = "";

        try 
        {
            // Validar e intentar obtener los datos de la comunicación.
            validarPrimeraLinea(partesLineaInicial);

            int ordTipoDeEvento = Integer.parseInt(partesLineaInicial[1]);

            if (ordTipoDeEvento < 0 || ordTipoDeEvento > TipoDeEvento.values().length)
            {
                throw new IllegalArgumentException("El tipo de evento no es soportado.");
            }

            int longitud = Integer.parseInt(partesLineaInicial[2]);
            int idUsuario = Integer.parseInt(partesLineaInicial[3]);

            return new Comunicacion(ordTipoDeEvento, true, longitud, idUsuario, "");
        } 
        catch (NumberFormatException e) 
        {
            mensajeErr = "Error: la peticion no tiene un formato correcto.";
            
        } catch (IllegalArgumentException ex) {
            mensajeErr = "Error de formato en comunicacion: " +  ex.getMessage();
        }

        return new Comunicacion(
            TipoDeEvento.ERROR_CLIENTE.ordinal(), 
            false, 
            mensajeErr.length(), 
            0, 
            mensajeErr
        );
    }

    @Override
    public String toString() 
    {
        String lineaInicial = String.format(
            "%s %d %s %d %d", 
            IDENTIFICADOR_COM, 
            tipoDeEvento, 
            String.valueOf(fueExitosa), 
            longitudCuerpo, 
            idUsuarioCliente
        );

        return String.format("%s\n%s", lineaInicial, cuerpoJSON);
    }

    public boolean tieneJson() 
    {
        return this.longitudCuerpo > 0;
    }

    public boolean tieneError() 
    {
        return tipoDeEvento == TipoDeEvento.ERROR_CLIENTE.ordinal() ||
               tipoDeEvento == TipoDeEvento.ERROR_SERVIDOR.ordinal();
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

        try 
        {
            validarLongitudCuerpo(longitudCuerpo, cuerpoJSON);

            this.cuerpoJSON = cuerpoJSON;

        } catch (IllegalArgumentException e) {  
            String mensajeErr = "Error de formato en comunicacion: " +  e.getMessage();
            
            this.fueExitosa = false;
            this.longitudCuerpo = mensajeErr.length();
            this.cuerpoJSON = mensajeErr;
        }
    }
}
