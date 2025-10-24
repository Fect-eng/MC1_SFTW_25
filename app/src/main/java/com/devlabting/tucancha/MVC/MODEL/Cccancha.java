package com.devlabting.tucancha.MVC.MODEL;

public class Cccancha {
    private String nombre;
    private String ubicacion;
    private String celular;
    private String tipoDoc;
    private String numDoc;
    private String horaDesde;
    private String horaHasta;
    private String tarifaDia;
    private String tarifaNoche;
    private long timestamp;

    // 🔹 Constructor vacío (obligatorio para Firebase)
    public Cccancha() {}

    // 🔹 Constructor con parámetros (lo usas en RegistroCancha_Activity)
    public Cccancha(String nombre, String ubicacion, String celular, String tipoDoc, String numDoc,
                    String horaDesde, String horaHasta, String tarifaDia, String tarifaNoche, long timestamp) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.celular = celular;
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.tarifaDia = tarifaDia;
        this.tarifaNoche = tarifaNoche;
        this.timestamp = timestamp;
    }

    // 🔹 Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(String tipoDoc) { this.tipoDoc = tipoDoc; }

    public String getNumDoc() { return numDoc; }
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }

    public String getHoraDesde() { return horaDesde; }
    public void setHoraDesde(String horaDesde) { this.horaDesde = horaDesde; }

    public String getHoraHasta() { return horaHasta; }
    public void setHoraHasta(String horaHasta) { this.horaHasta = horaHasta; }

    public String getTarifaDia() { return tarifaDia; }
    public void setTarifaDia(String tarifaDia) { this.tarifaDia = tarifaDia; }

    public String getTarifaNoche() { return tarifaNoche; }
    public void setTarifaNoche(String tarifaNoche) { this.tarifaNoche = tarifaNoche; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    private double lat;
    private double lng;

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

}
