package com.example.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Datos de envío que el cliente escribe en el checkout. Se guardan junto a la boleta,
// así si el usuario cambia su dirección después, la orden antigua conserva la dirección original.
@Embeddable
public class DatosEnvio {

    @Column(length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 150)
    private String correo;

    @Column(length = 150)
    private String calle;

    @Column(length = 50)
    private String departamento;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String comuna;

    @Column(length = 255)
    private String indicaciones;

    public DatosEnvio() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
}