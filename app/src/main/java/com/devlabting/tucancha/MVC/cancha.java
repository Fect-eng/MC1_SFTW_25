package com.devlabting.tucancha.MVC;

public class cancha {
    private String nombre;
    private String direccion;
    private String celular;
    private String horario;
    private double lat;
    private double lng;

    public cancha(String nombre, String direccion, String celular, String horario, double lat, double lng) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.celular = celular;
        this.horario = horario;
        this.lat = lat;
        this.lng = lng;
    }

    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getCelular() { return celular; }
    public String getHorario() { return horario; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
