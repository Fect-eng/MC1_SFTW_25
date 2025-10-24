package com.devlabting.tucancha.MVC.MODEL;

public class CanchaModel {
    private String nombre;
    private double lat, lng;

    public CanchaModel(String nombre, double lat, double lng) {
        this.nombre = nombre;
        this.lat = lat;
        this.lng = lng;
    }
    public String getNombre() { return nombre; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
