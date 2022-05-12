package com.pasarceti.chat.servidor.modelos;

/**
 * Interpreta un String con una linea de encabezado con el protocolo del chat y
 * facilita procesar la accion del cliente.
 * 
 */
public class AccionCliente extends Comunicacion 
{
    private final TipoDeAccion tipoDeAccion;

    public AccionCliente(int tipoDeAccion, int longitud, int idUsuarioCliente, String cuerpoJSON) 
    {
        // Una solicitud de acción es exitosa por defecto.
        super(true, longitud, idUsuarioCliente, cuerpoJSON);

        // Especificar el tipo de acción del cliente.
        this.tipoDeAccion = TipoDeAccion.values()[tipoDeAccion];
    }

    public static AccionCliente desdeEncabezado(String lineaEncabezado) throws NumberFormatException, IllegalArgumentException
    {
        String[] partesLineaInicial = lineaEncabezado.split(" ");

        // Validar e intentar obtener los datos de la comunicación.
        validarPrimeraLinea(partesLineaInicial);

        int ordTipoDeEvento = Integer.parseInt(partesLineaInicial[1]);

        if (ordTipoDeEvento < 0 || ordTipoDeEvento > TipoDeEvento.values().length)
        {
            throw new IllegalArgumentException("El tipo de evento no es soportado.");
        }

        int longitud = Integer.parseInt(partesLineaInicial[2]);
        int idUsuario = Integer.parseInt(partesLineaInicial[3]);

        return new AccionCliente(ordTipoDeEvento, longitud, idUsuario, "");

//        try 
//        {
//        } 
//        catch (NumberFormatException e) 
//        {
//            mensajeErr = "Error: la peticion no tiene un formato correcto.";
//            
//        } catch (IllegalArgumentException ex) {
//            mensajeErr = "Error de formato en comunicacion: " +  ex.getMessage();
//        }
    }

    @Override
    public String toString() 
    {
        String lineaEncabezado = String.format(
            "Acción del cliente: %s %d %s %d %d", 
            IDENTIFICADOR_COM, 
            tipoDeAccion.ordinal(), 
            String.valueOf(fueExitosa), 
            longitudCuerpo, 
            idUsuarioCliente
        );

        return String.format("%s\n%s", lineaEncabezado, cuerpoJSON);
    }
    
    public static boolean esAccionDeAutenticacion(TipoDeAccion tipoDeAccion) {
        return tipoDeAccion == TipoDeAccion.INICIAR_SESION || 
               tipoDeAccion == TipoDeAccion.REGISTRAR_USUARIO;
    }
    
    public boolean esAccionDeAutenticacion() {
        return esAccionDeAutenticacion(this.tipoDeAccion);
    }

    public TipoDeAccion getTipoDeAccion() {
        return tipoDeAccion;
    }
}
