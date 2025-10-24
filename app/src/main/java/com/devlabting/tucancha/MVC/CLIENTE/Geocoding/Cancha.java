package com.devlabting.tucancha.MVC.CLIENTE.Geocoding;

public class Cancha {
    private String id;
    private String nombre;
    private String direccion;

    // Constructor vacío
    public Cancha() {}

    // Constructor con parámetros
    public Cancha(String id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
