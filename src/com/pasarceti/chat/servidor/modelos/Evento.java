
package com.pasarceti.chat.servidor.modelos;

import java.sql.Date;

public class Evento {
    
    private int id; 
    private Date fecha;
    private String datos;

    public Evento() {
    }

    public Evento(int id, Date fecha, String datos) {
        this.id = id;
        this.fecha = fecha;
        this.datos = datos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }

}
